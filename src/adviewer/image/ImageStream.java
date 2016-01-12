package adviewer.image;

import adviewer.gui.ADWindow;
import adviewer.gui.ImagePanel;
import adviewer.util.CameraConfig;
import adviewer.util.Log;
import ij.IJ;
import ij.ImageJ;
import ij.ImageStack;
import ij.process.ImageProcessor;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;

import com.charliemouse.cambozola.shared.CamStream;
import com.charliemouse.cambozola.shared.ExceptionReporter;
import com.charliemouse.cambozola.shared.ImageChangeEvent;
import com.charliemouse.cambozola.shared.ImageChangeListener;
import ij.ImagePlus;
import ij.process.ByteProcessor;


/**
 * adviewer.image.ImageStream.java is a super class for Image Streams, it is not intended to be used 
 * directly, but instead meant to be sub classed. The sub classes will Override 
 * the run method for thread and is responsible for updating a stream
 * buffer of some kind. 
 * 
 * @author Brian Freeman
 */
public class ImageStream extends Thread {

    public ImagePlusPlus impp;
    public ImageJ ij; //reference to main ImageJ class
    public Image img;
    public Stream streamer;
    private ImagePanel imagePanel;

    public int[] pixels;
    public byte[] pixelBytes;

    public int[] savedPixels;
    public byte[] savedPixelBytes;

    public int imageWidth;
    public int imageHeight;
    public int imageSize;

    public int frameId;
    public String type;
    public String name; //name of this stream (camera name)

    public boolean processing;
    public boolean isConnected;
    public boolean returnImage; // if we want the buffered image instead of ImagePlusPlus   

    public int prevUpdateCounter;
    public int updateCounter;

    public ImageIcon icon;

    public JLabel label;
    public JPanel jp;

    public boolean isNewStack;
    public boolean show = false;

    public int sleepTime = 0;
    public ImageStack imageStack;
    public boolean isSaveToStack = false;

    public int threshold = 0;
    public boolean isFiltering;
    public boolean isSubtraction;

    public long time;
    public long prevTime;

    public double fps;
    public String fpsFormatted;
    public double viewableFPS;
    public int viewableFPSUpdateCounter;

    public Timer timer; //timer used for fps calc
    public int timerDelay = 1000; //delay for above timer

    // These are used for the frames/second calculation
    public int numImageUpdates;
    public Double realFPS;
    public String rFPS;

    public Date date;

    public Properties properties = new Properties();

    public String fpsViewableFormatted;

    public NumberFormat form;
    public String PROPERTIESFILE = "ADViewer.properties";

    //frame rate options = less or more bandwidth
    public final int FULL_FPS = 0;
    public final int NINETY_FPS = 5;
    public final int SIXTY_FPS = 15;
    public final int TWENTY_FIVE_FPS = 22;
    public final int FIFTEEN_FPS = 60;
    public final int ONE_FPS = 1000;
    public int SLEEPTIME = 30;

    protected ExceptionReporter reporter = null;
    protected Vector listeners;
    protected Toolkit tk;
    protected URL stream;
    protected String userpassEncoded;
    protected URL docBase;
    protected DataInputStream inputStream = null;
    protected boolean isDefunct = false;
    protected boolean collecting = false;
    protected byte[] rawImage;
    protected String imageType = "image/jpeg";
    protected long startTime = 0;
    protected int imgidx = 0;
    protected int retryCount = 1;
    protected int retryDelay = 1000;
    protected String appName = "";
    protected boolean debug = false;

    public CameraConfig cam;
    public boolean isCEBAFBC = false;
    public boolean isLERFBC = false;
    

    ImageStream(CameraConfig cam) {
        this.cam = cam;
        init();
    }

    ImageStream(ImagePlusPlus impp , CameraConfig cam) {
        this.impp = impp;
        this.cam = cam;
        init();
    }

    public void init() {
        //ImageJ instance
        ij = IJ.getInstance();
        tk = Toolkit.getDefaultToolkit();
        listeners = new Vector();

        frameId = 0;

        isConnected = false;
        processing = false;

        prevUpdateCounter = 0;
        updateCounter = 0;

        date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("YMMDD_HHmmss");
        
        prevTime = date.getTime();

        numImageUpdates = 0;
        viewableFPSUpdateCounter = 0;
        viewableFPS = 0;

        jp = new JPanel();
        label = new JLabel();
        icon = new ImageIcon();

        label.setIcon(icon);
        jp.add(label);

        
        //timer setup
        timer = setFPSTimer();

        form = DecimalFormat.getInstance();
        ((DecimalFormat) form).applyPattern("0.0");

        timer.start();
        
        String name = cam.getId()+"_"+ format.format(date);
        this.setName( name );
        Log.log("Thread named = " + name , debug);
        
        
    }
    
    public Timer setFPSTimer(){
        return ( new Timer(timerDelay, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
				// TODO Auto-generated method stub

                //sets the title on the title bar
                setImageTitle();
                //do frames per sec calc
                doFpsCalc();

                //set the prevTime to the time set in doFpsCalc()
                prevTime = time;
                //set numImageUpdates to 0 to reset counter
                numImageUpdates = 0;
                updateCounter = 0;

                if (impp != null && imagePanel!=null ) {
                    if(imagePanel.mainPanel!=null && imagePanel.mainPanel.fps!=null){
                        imagePanel.mainPanel.fps.setText(fpsFormatted + " fps");
                    }
                }
            }
        
        }) );
    }

    public void addImageChangeListener(ImageChangeListener cl) {
        listeners.addElement(cl);
    }

    public void removeImageChangeListener(ImageChangeListener cl) {
        listeners.removeElement(cl);
    }

    public void fireImageChange() {
        // TODO Auto-generated method stub
        ImageChangeEvent ce = new ImageChangeEvent(this);
        for (Enumeration e = listeners.elements(); e.hasMoreElements();) {
            ((ImageChangeListener) e.nextElement()).imageChanged(ce);
        }
    }

    /**
     * Writes the title to the title bar in the image window
     */
    public void setImageTitle() {
        Date date = new Date();
        SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

        String s = cam.getName();
        s += "        ";
        s += simpleDate.format(date);

        if (frameId != 0) {
            s += "        ";
            s += "Frame Id: " + frameId;
        }

        if (impp != null) {
            impp.setTitle(s);
        }
        //	+ "               Unique Frame ID:" + getUniqueId());
        //Log.log( this.getName() + " - " +s , debug);
    }

    public void slowDown(int i) {

        sleepTime = (int) this.sleepTime + i;

        System.out.println("Thread will sleep for .... " + ((double) this.sleepTime / 1000.0) + " seconds");

    }

    public void speedUp(int i) {
        if (this.sleepTime != i) {
            this.sleepTime = (int) this.sleepTime - i;
        }
        System.out.println("Thread will sleep for .... " + ((double) this.sleepTime / 1000.0) + " seconds");

    }

    public void captureOn() {
        //stack capture on
        if (impp == null) {
            Log.log(" IMPP is null ", debug);
            return;
        }
        else{
            this.impp = imagePanel.impp;
        }

        //imagePanel.timer.stop();

        //IndexColorModel cmodel = impp.cm; //get ImagePlusPlus color model
        ImageProcessor ip = impp.getProcessor();

        imageStack = new ImageStack(impp.getWidth(), impp.getHeight());
        imageStack.addSlice(impp.getTitle(), ip);
        //imageStack.setColorModel(cmodel);
        // Note: we need to add this first slice twice in order to get the slider bar 
        // on the window - ImageJ won't put it there if there is only 1 slice.
        imageStack.addSlice(impp.getTitle(), ip);
        imagePanel.impp.setStack(imageStack);
        this.impp = imagePanel.impp;
        //impp.close();

        //ImagePlusPlus impp2 = new ImagePlusPlus(impp.getTitle(), imageStack, this);
        //impp2.setProcessor(ip);
        
        //impp2.setImagePanel(impp.imagePanel);
        //impp2.showMe();
        //impp2.setWindow(impp.getWindow());
        //impp.getProcessor().setLut(new LUT(cmodel, img.getWidth(), img.getHeight()));
        // this.impp = impp;
        //imagePanel.impp = impp2;
        //this.impp = imagePanel.impp;
        //impp.showMe();
        Log.log("impp stack size = " + imagePanel.impp.getStackSize(),debug);

        //impp.setImagePanel(this.imagePanel);
        //this.imagePanel.impp = impp;
        //this.impp.myWindow = (ADWindow) impp.getWindow();
        //give reference to impp to the streamer and then 
        //make sure impp knows what is updating it
       // this.impp.setImageUpdater(this);
        //impp.setImagePanel(impp.getImagePanel());   //pass a reference to ImagePanel in ImagePlusPlus
        //setImagePanel(impp.getImagePanel()); //pass an ImagePanel reference to the streamer
        imagePanel.timer.start();
        Log.log("Done ...", debug);
    }

    public void captureOff() {
        isSaveToStack = false;
    }

    public void makeImageCopy(String more) {
        ImageProcessor dipcopy = impp.getProcessor().duplicate();
        ImagePlus imgcopy = new ImagePlusPlus(impp.getTitle(), dipcopy, false);
        imgcopy.show();
    }

    public void saveBackground() {
        if (impp == null && img != null) {
            //return pixel array from image
            return;
        }
        if (impp.getProcessor().getPixels() instanceof int[]) {
            this.savedPixels = (int[]) impp.getProcessor().getPixels();
        } else if (impp.getProcessor().getPixels() instanceof byte[]) {
            this.savedPixelBytes = (byte[]) impp.getProcessor().getPixels();
        }

    }

    public int[] subtraction(int[] pix) {

        if (savedPixels == null && savedPixels.length == 0 && savedPixels.length != pix.length) {
            return pix;
        }

        this.pixels = new int[pix.length];

        for (int i = 0; i < pixels.length; i++) {

            this.pixels[i] = Math.abs(pix[i] - savedPixels[i]);

        }

        return this.pixels;

    }

    public byte[] subtraction(byte[] pix) {

        if (savedPixelBytes == null && savedPixelBytes.length == 0 && savedPixelBytes.length != pix.length) {
            return pix;
        }

        this.pixelBytes = new byte[pix.length];

        //could make this a function to print incoming pixel values to a file.
        //for(int j = 0 ; j < pixelBytes.length; j++){
        //writer.print((int) ( pix[j] & 0xFF ) + " ");
        //if( j % getImageWidth() == 0 ){
        //writer.print("\n");
        //}
        //}
        //writer.print("\n************************************************************************************\n");
        for (int i = 0; i < pixelBytes.length; i++) {

            // when the byte to int conversion is done it is a signed int. Have to use the 0xFF bit mask
            //to convert them to unsigned int, then do the math
            int x = Math.abs((pix[i] & 0xFF) - (savedPixelBytes[i] & 0xFF));

            if (x > threshold) {
                this.pixelBytes[i] = (byte) (x);
            }

        }

        return this.pixelBytes;

    }

    /**
     * Method to calculate the Frames per second. This is only what is being
     * captured from epics, and not necessarily the viewable frame rate.
     */
    public void doFpsCalc() {

        //prevTime = time;
        time = new Date().getTime();

        //diff should be equal to TIMERWAIT time, since this is what triggers an update
        double diff = time - prevTime;

        //1s = 1000 so have to multiply numImageUpdates by 1000
        fps = 1000 * (numImageUpdates / diff);
        viewableFPS = 1000 * this.updateCounter / diff;

        fpsFormatted = form.format(fps);
        fpsViewableFormatted = form.format(viewableFPS);

        String s = "===============================================\n";
        s += "time = " + time + " and  prevTime =  " + prevTime + "\n";
        s += "diff = " + diff + "\n";
        s += "numUpdates = " + numImageUpdates + "\n";
        s += "fps = " + fps + "\n";
        s += "fpsFormatted = " + fpsFormatted + "\n";
        s += "===============================================\n";

        //Log.log(s , debug);
        //  IJ.log(s);
    }

    /**
     * Update an impp based on another image plus plus
     *
     * @param impp2
     */
    public ImagePlusPlus updateImpp(ImagePlusPlus impp, ImagePlusPlus impp2) {
        //converting first to 8-bit version of the RGB
        //Log.log("impp stack size =  " + impp.getStackSize(), debug);
        //if(impp.getStack().getSize() >1 ) Log.log("stack is greater than 1 " + impp.getStack().getSize() , debug);
        if (impp.getWindow() != null && !impp.isClosed()) {
            ImageProcessor ipr = impp2.getProcessor().convertToByte(true); //create new generic image processor, with 8 bit color map
            ipr.setLut(impp.getProcessor().getLut());                      // give the generic processor, impp's LUT
            ipr.gamma( impp.gamma );                                        //change the ipr gamma thats deined in impp
            
            
            //pixels = (byte []) getImpp().getProcessor().getPixels();
            //pixels = imageBytes;
            //System.out.println( impp.getProcessor().toString() + " and " +  impp2.getProcessor().toString() );
            pixelBytes = (byte[]) ipr.getPixels();

            if (this.isSubtraction & savedPixelBytes != null && savedPixelBytes.length > 0) {
                pixelBytes = subtraction(pixelBytes);
            }

            impp.setProcessor(ipr);

            impp.getProcessor().setPixels(pixelBytes);
            
            if(impp.findedges) IJ.runPlugIn(impp,"ij.plugin.filter.Filters" ,"edge" );
            
            
            //if stacking
            if (isSaveToStack) {
                Log.log("isSaveToStack ..... stack size =  " + impp.getStackSize(), debug);
               // impp.setSlice(impp.getNSlices());
                impp.getStack().addSlice("#"+impp.getStackSize(), impp.getProcessor().duplicate());
                //impp.getStack().addSlice(name, ipr.getPixels());
            }
            //else{
                //impp.setSlice(impp.getNSlices());
               // impp.updateAndDraw();
            //}
            /*super.numImageUpdates++;
             super.setFrameId(super.getFrameId()+1);
             if(super.getFrameId() > 1000000){
             super.setFrameId(1);
             }*/
            return impp;
        } else {
            Log.log("Window is null = "+impp.getWindow()
                    +"\n//or impp is Closed .... " + impp.isClosed + "...stopping ...", debug);
            //this.stop();
            return impp;
        }
    }

    public ImagePanel getImagePanel() {
        return imagePanel;
    }

    public void setImagePanel(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    public synchronized byte[] getRawImage() {
        if (streamer == null) {
            return null;
        }
        return streamer.getRawImage();
    }

    public synchronized Image getCurrent() {
        if (streamer == null) {
            return null;
        }
        return streamer.getCurrent();
    }

    public synchronized ImagePlusPlus getImagePlusPlus() {
        if (streamer == null) {
            return null;
        }
        return streamer.getImagePlusPlus();
    }

    public void kill() {
        this.timer.stop();
        
        
        
        isDefunct = true;
        collecting = false;
        this.processing =false;
        
        
        Log.log("Killed " + this.getName(), debug);
        
    }

    public ImagePlusPlus updateImpp() {

        return this.impp;
    }

    public void setSleepTime(int sleepTime) {
        this.imagePanel.timer.setDelay(sleepTime);
    }

}
