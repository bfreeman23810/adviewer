package adviewer.image;

import adviewer.gui.ADWindow;
import adviewer.gui.ImagePanel;
import ij.IJ;
import ij.ImageJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.PlotWindow;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.io.SaveDialog;
import ij.macro.Interpreter;
import ij.measure.Calibration;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Label;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.Point;
import java.awt.PopupMenu;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.IndexColorModel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import adviewer.gui.Menus;
import adviewer.util.LUTCollection;
import adviewer.util.Log;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * ImagePlusPlus.java inherits from ImageJ's ImagePlus class.
 *
 * @author Brian Freeman
 *
 */
public class ImagePlusPlus extends ImagePlus {

    //This object
    public ImagePlusPlus impp;
	
    public ImageStream imageUpdater;

    ImageJ ij;

    //Menu used to create the right click context menu
    PopupMenu popup;
    public Menus mainmenu;

    public Dimension d;

    //Canvas
    public ImageCanvas myCanvas;
    public ImageWindow myWindow;
    public ImagePanel imagePanel;
    public ADWindow adwin;
    public boolean debug =false;
    
    boolean isClosed;

    public IndexColorModel cm;
    public LUT lut;
    public LUT orginalLUT;
    public Toolbar tb;
    public ImageProcessor imageProcessor;

    public MouseListener popupListener;
    public LUTCollection luts;

    /*MenuItem start;
     MenuItem stop;
     MenuItem snap;
     Menu capture;
     MenuItem captureOn;
     MenuItem captureOff;*/
    boolean addListeners = true;

    /*ArrayList<CheckboxMenuItem> cmenuItems;
     ArrayList<CheckboxMenuItem> fpsChecks;*/
    Panel statusPanel;
    Label statusLabel_1;
    Label statusLabel_2;

    boolean isShowingIJ = false;
    public int imageType = GRAY8;
    public int default16bitDisplayRange;
    public static Vector mylisteners = new Vector();

    public double gamma = 1;
    public boolean findedges = false;
    private byte[] pixelBytes;
    
	//several different ways to instantiate this object
    //Tring to exploit all of ImagePlus constructors
    public ImagePlusPlus() {
        super();
        init();
    }

    public ImagePlusPlus(String pathOrUrl) {
        super(pathOrUrl);
        init();
    }
    
     public ImagePlusPlus(String pathOrUrl , LUTCollection luts) {
        super(pathOrUrl);
        this.luts = luts;
        init();
    }

    public ImagePlusPlus(String title, Image img) {
        super(title, img);
        init();
    }

    public ImagePlusPlus(String text, ImageProcessor ip) {
        super(text, ip);
        init();
    }

    public ImagePlusPlus(String text, ImageProcessor ip, boolean addListeners) {
        super(text, ip);
        this.addListeners = addListeners;
        init();
    }

    public ImagePlusPlus(String text, ImageStack is) {
        super(text, is);
        init();
    }

    public ImagePlusPlus(String pathOrUrl, ImageStream updater) {
        super(pathOrUrl);
        imageUpdater = updater;
        init();
    }

    public ImagePlusPlus(ImageStream updater) {
        super();
        imageUpdater = updater;
        init();
    }

    public ImagePlusPlus(String title, Image img, ImageStream updater) {
        super(title, img);
        imageUpdater = updater;
        init();
    }
    
     public ImagePlusPlus(String title, Image img, ImageStream updater, LUTCollection luts) {
        super(title, img);
        imageUpdater = updater;
        this.luts = luts;
        init();
    }

    public ImagePlusPlus(String text, ImageProcessor ip, ImageStream updater) {
        super(text, ip);
        imageUpdater = updater;
        init();
    }
    
    public ImagePlusPlus(String text, ImageProcessor ip, ImageStream updater, LUTCollection luts) {
        super(text, ip);
        imageUpdater = updater;
        this.luts = luts;
        init();
    }

    public ImagePlusPlus(String text, ImageStack is, ImageStream updater) {
        super(text, is);
        imageUpdater = updater;
        init();
    }

    public ImagePlusPlus(ImagePlus imp, ImageStream updater) {
        // TODO Auto-generated constructor stub
        super();
        imageUpdater = updater;
        init();
    }

    //check to see if the image object is closed
    public boolean isClosed() {

        isClosed = (this.isVisible() && this.getWindow() == null && this.getCanvas() == null);
        return isClosed;
    }

    /**
     * Instantiate all the global variables
     */
    public void init() {

        //references to this images containers
        this.myCanvas = this.getCanvas();
        this.myWindow = (ADWindow) this.getWindow();
        
        
        
        if(luts !=null) mainmenu = new Menus(this, luts);
       // else mainmenu = new Menus(this);
        
        if (this.imageUpdater != null && mainmenu !=null ) {
            mainmenu.setImageUpdater(imageUpdater); // make sure the menu knows that is has an updater
        }

        popupListener = new PopupListener();
        
        if(mainmenu!=null) popup = addContextMenu();
        isClosed = true;

        //reference to self
        this.impp = this;
        

        try {
            orginalLUT = this.getProcessor().getLut();
        } catch (Exception e) {
            Log.log("Problem with creating the original LUT .... " + e.getMessage() , debug);
        }

        imageProcessor = this.getProcessor();
       // pixelBytes = (byte[]) imageProcessor.getPixels();
        
        tb = new Toolbar();
        ij = IJ.getInstance();

       if (ij == null) {
            Log.log("new ImageJ instance created ... " );
            ij = new ImageJ(ImageJ.NO_SHOW);
            
            
        }

        

    }

//    public void resetPixels(){
//        
//        imageProcessor.setPixels(pixelBytes);
//        
//        this.updateAndDraw();
//        Log.log(""+pixelBytes.length , debug );
//    }
    
    /**
     * getPath was copied from a private function in ij.io.FileSaver
     *
     * @param type
     * @param extension
     * @return
     */
    public String getPath(String type, String extension) {
        String name = this.getTitle();
        SaveDialog sd = new SaveDialog("Save as " + type, name, extension);
        name = sd.getFileName();
        if (name == null) {
            return null;
        }
        String directory = sd.getDirectory();
        this.startTiming();
        String path = directory + name;
        return path;
    }

    public void resetDisplayRange() {
        if (imageType == GRAY16 && default16bitDisplayRange >= 8 && default16bitDisplayRange <= 16 && !(getCalibration().isSigned16Bit())) {
            ip.setMinAndMax(0, Math.pow(2, default16bitDisplayRange) - 1);
        } else {
            ip.resetMinAndMax();
        }
    }

    /**
     * A version of show() from ImagePlus, need to to do this so I can add it to
     * an ADWindow instead of an ImageWindow. Attempting to gain more control of
     * Window behavior
     */
    public void showMe() {

        if (isVisible()) {
            Log.log("ShowMe() is exiting, must be visable ... ", debug);
            return;
        }
        //win = null;
        if ((IJ.isMacro() && ij == null) || Interpreter.isBatchMode()) {
            //if (isComposite()) ((CompositeImage)this).reset();
            Log.log("...in get Current Image");
            ImagePlusPlus img = (ImagePlusPlus) WindowManager.getCurrentImage();
            if (img != null) {
                img.saveRoi();
            }
            WindowManager.setTempCurrentImage(this);
            Interpreter.addBatchModeImage(this);
            return;
        }
		//if (Prefs.useInvertingLut && getBitDepth()==8 && ip!=null && !ip.isInvertedLut()&& !ip.isColorLut())
        //invertLookupTable();
        img =  getImage();
        if ((img != null) && (width >= 0) && (height >= 0)) {
            //activated = false;
            int stackSize = getStackSize();
			//if (compositeImage) stackSize /= nChannels;
			/*if (stackSize>1)
             win = new StackWindow(this);
             else
             win = new ImageWindow(this);*/
            if (roi != null) {
                roi.setImage(this);
            }
            if (getOverlay() != null && getCanvas() != null) {
                getCanvas().setOverlay(getOverlay());
            }
			//draw();
            //IJ.showStatus(statusMessage);

            if (imageType == GRAY16 && default16bitDisplayRange != 0) {
                resetDisplayRange();
                updateAndDraw();
            }
            if (stackSize > 1) {
                Log.log("stackSize > 1", debug);
                int c = getChannel();
                int z = getSlice();
                int t = getFrame();
                if (c > 1 || z > 1 || t > 1) {
                    setPosition(c, z, t);
                }
            }
        }

        //add my custom Context menu (right mouse click)
        addMenu();

        if (addListeners) {
            //addWindowListeners();
            addCanvasListeners();
        }

        
        isClosed = false;
        notifyListeners(OPENED);
        this.unlock();
        
        if(ij !=null){
            
             IJ.setDebugMode(debug);
             Log.log( "ij.debug =  "+IJ.debugMode + " AND debug = " + debug);
            Log.log(ij.getInfo());
        }
    }

    /*Override the show() method from ImagePlus */
    public void show(String statusMessage) {

        if (this.isVisible()) {
            return;
        }

        super.show(statusMessage);

        if (this.getCanvas() == null || this.getWindow() == null) {
            return;
        }
        if (this.getWindow() != null) {
            addMenu();

	
            this.getWindow().pack();

			//Overide window params  
            //this.getWindow().isShowing();
            if (addListeners) {

                addCanvasListeners();
            }
            isClosed = false;
            Log.log("Showing ..... isClosed = " + isClosed, debug);
        }
		//System.out.println("Showing.......");

    }

    public void setFPSText(String fps) {
        if(imagePanel.mainPanel.fps != null){
            imagePanel.mainPanel.fps.setText(fps + " fps");
    
        }
        else return;
    }

    private void addCanvasListeners() {
        // TODO Auto-generated method stub
        if (this.getCanvas() == null) {
            return;
        }

        //add key listeners
        this.getCanvas().addKeyListener(new KeyAdapter() {
            int i = 0;

            @Override
            public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub

            }

            @Override
            public void keyReleased(KeyEvent e) {
				// TODO Auto-generated method stub

            }

            @Override
            public void keyPressed(KeyEvent e) {
					// TODO Auto-generated method stub

                char key = e.getKeyChar();
                int code = e.getKeyCode();

                Log.log(key + " : " + code , debug);

                switch (code) {

                    case 77: //measure (Ctrl + M) 
                        if (e.isControlDown()) {
                            IJ.runPlugIn(impp, "ij.plugin.filter.Analyzer", "Measure");
                            System.out.println("Measureing ... ");
                        }
                        break;
                    case 57: //slow down Ctrl + 9
                        if (e.isControlDown()) {
                            if (imageUpdater != null) {
									//if(i>0 && i < imageUpdater.options.length-1){
                                //i--;
                                //}
                                //System.out.println(i+"");

									//imageUpdater.setSleepTime(imageUpdater.options[i]);
                                //System.out.println(imageUpdater.options[i] + "FPS");
                                //imageUpdater.slowDown(10);
                                //imageUpdater.threshold +=10;
                                //System.out.println("Threhold = "+imageUpdater.threshold);
                            }
                        }
                        break;
                    case 48: //speed up Ctrl + 0
                        if (e.isControlDown()) {
                            if (imageUpdater != null) {
									//imageUpdater.speedUp(10);
                                //imageUpdater.threshold -=10;
                                //System.out.println("Threhold = "+imageUpdater.threshold);

                            }
                        }
                        break;

                    case 71: //grap (snap) Ctrl + g
                        if (e.isControlDown()) {
                            if (imageUpdater != null) {
                                Date date = new Date();
                                SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yy HH:mm:ss");
                                imageUpdater.makeImageCopy("");

                            }
                        }
                        break;

                }

            }
        });

    }

    /**
     * Add context menu arg0
     */
    public void addMenu() {

        //this.add(popup);
        if (this.getCanvas() != null) {
            this.getCanvas().add(popup);
            this.getCanvas().addMouseListener(popupListener);
        } else {
            Log.log("ImageCanvas was null" , debug);
        }
    }

    public PopupMenu addContextMenu() {
        PopupMenu menu = new PopupMenu("Image Menu");

		//only add this to the menu if we have an image handler
        //if(imageHandler != null){
			/*section for on menu controls*/
			//start playing
        menu.add(mainmenu.createSaveAsMenu());

        menu.addSeparator();
        if (imageUpdater != null) {
            //Log.log("adding stream menu ... " , );
            mainmenu.addImageUpdaterItems(menu);
        }

        //brightness and contrast
        MenuItem bAndC = new MenuItem("Brightness/Contrast...");
        bAndC.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                IJ.run("Brightness/Contrast...");
            }
        });
        menu.add(bAndC);
        menu.addSeparator();

        //add a menu of false color maps (LUTs) 
        Menu luts = mainmenu.createLUTMenu();
        menu.add(luts);
        menu.addSeparator();

        Menu tools = mainmenu.createToolMenu();
        menu.add(tools);
        menu.addSeparator();

        //measuments menu, for next version
        Menu measure = mainmenu.createMeasurementsMenu();
        menu.add(measure);
        menu.addSeparator();

        MenuItem imagej = new MenuItem("Open ImageJ");
        imagej.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub	
                ij = new ImageJ();
                isShowingIJ = true;

            }
        });
        menu.add(imagej);
        menu.addSeparator();

        if (imageUpdater != null) {
            Menu tmenu = mainmenu.createThrottleMenu();
            menu.add(tmenu);

            menu.addSeparator();

            Menu imageProMenu = mainmenu.createImageManipMenu();
            menu.add(imageProMenu);
        } else {
            //System.out.println("Updater is NULL");
        }

        return menu;
    }

    //mouse moved override
    @Override
    public void mouseMoved(int x, int y) {
        if (ij != null && imagePanel!=null) {
            if(imagePanel.mainPanel.pixelInspector!=null) imagePanel.mainPanel.pixelInspector.setText(getLocationAsString(x, y) + getValueAsString(x, y));
        }
        savex = x;
        savey = y;

    }

    
   
    //used to save cursor positions
    public int savex, savey;

    //Overridden from ImagePlus to make it public
    public String getValueAsString(int x, int y) {
        if (win != null && win instanceof PlotWindow) {
            return "";
        }
        Calibration cal = getCalibration();
        int[] v = getPixel(x, y);
        int type = getType();
        switch (type) {
            case GRAY8:
            case GRAY16:
            case COLOR_256:
                if (type == COLOR_256) {
                    if (cal.getCValue(v[3]) == v[3]) // not calibrated
                    {
                        return (", index=" + v[3] + ", value=" + v[0] + "," + v[1] + "," + v[2]);
                    } else {
                        v[0] = v[3];
                    }
                }
                double cValue = cal.getCValue(v[0]);
                if (cValue == v[0]) {
                    return (", value=" + v[0]);
                } else {
                    return (", value=" + IJ.d2s(cValue) + " (" + v[0] + ")");
                }
            case GRAY32:
                double value = Float.intBitsToFloat(v[0]);
                String s = (int) value == value ? IJ.d2s(value, 0) + ".0" : IJ.d2s(value, 4, 7);
                return (", value=" + s);
            case COLOR_RGB:
                return (", value=" + v[0] + "," + v[1] + "," + v[2]);
            default:
                return ("");
        }
    }

    class PopupListener extends MouseAdapter {

        public void mousePressed(MouseEvent e) {
            maybeShowPopup(e);
        }

        public void mouseReleased(MouseEvent e) {
            maybeShowPopup(e);
        }

        private void maybeShowPopup(MouseEvent e) {
            if (e.isPopupTrigger()) {
                popup.show(e.getComponent(),
                        e.getX(), e.getY());
            }
        }
    }

    public ImagePanel getImagePanel() {
        return imagePanel;
    }

    public void setImagePanel(ImagePanel imagePanel) {
        this.imagePanel = imagePanel;
    }

    public ImageStream getImageUpdater() {
        return imageUpdater;
    }

    public void setImageUpdater(ImageStream imageUpdater) {
        this.imageUpdater = imageUpdater;
        Log.log("Stream has been set .... ",debug);
    }

    public void writeToStatus(String s) {
        //statusLabel_3.setText(s);
    }

    /**
     * return ROI selection as an array of integers
     *
     * @return
     */
    public int[] getRoiAsIntArray() {
        //get ROI
        Roi roi = impp.getRoi();
        //return array
        int[] roiPixels;

        //only do something if roi is not null, and the type is rectangle
        if (roi != null && roi.isArea() && roi.getType() == Roi.RECTANGLE) {
            System.out.println(roi.toString());

            //bounding rectangle
            Rectangle r = roi.getBounds();
            //upper left corner as point
            Point p = r.getLocation();

			//System.out.println( p.x +" : "+ p.y );
            int numElements = (int) (r.getWidth() * r.getHeight());

            roiPixels = new int[numElements];

            int ele = 0;
            for (int j = p.y; j < (r.getHeight() + p.y); j++) {

                for (int i = p.x; i < (r.getWidth() + p.x); i++) {

                    if (ele < numElements) {
                        roiPixels[ele] = (this.getProcessor().getPixel(i, j));

                        if (roiPixels[ele] < 0 || roiPixels[ele] > 255) {

                            int red = (roiPixels[ele] >> 16) & 0xFF;
                            int b = (roiPixels[ele] >> 8) & 0xFF;
                            int g = (roiPixels[ele]) & 0xFF;

                            roiPixels[ele] = (int) (0.3 * (double) red + 0.59 * (double) g + 0.11 * (double) b);

                        }

                        ele++;
                    }
                }

            }

        } else {
            writeToStatus("No ROI selected or unsupported ROI .... ");
            return null;
        }

        return roiPixels;
    }

    /**
     * return ROI selection as an array of integers
     *
     * @return
     */
    public void getRoiAsXSeries() {
        //get ROI
        Roi roi = impp.getRoi();
        //return array
        int[][] roiPixels;

        //only do something if roi is not null, and the type is rectangle
        if (roi != null && roi.isArea() && roi.getType() == Roi.RECTANGLE) {
            System.out.println(roi.toString());

            //bounding rectangle
            Rectangle r = roi.getBounds();
            //upper left corner as point
            Point p = r.getLocation();

			//System.out.println( p.x +" : "+ p.y );
            int numElements = (int) (r.getWidth() * r.getHeight());

            roiPixels = new int[this.getWidth()][this.getHeight()];

            int x = 0;
            int y = 0;
            for (int j = p.y; j < (r.getHeight() + p.y); j++) {

                for (int i = p.x; i < (r.getWidth() + p.x); i++) {

                    if (x * y < numElements) {
                        roiPixels[j][i] = (this.getProcessor().getPixel(i, j));

                    }

                }
            }

            for (int i = 0; i < roiPixels.length; i++) {

            }

        } else {
            writeToStatus("No ROI selected or unsupported ROI .... ");

        }

    }

    /**
     * public static void main(String[] args){ //ImagePlusPlus impp = new
     * ImagePlusPlus("/software/video/test/"); ImagePlusPlus impp = new
     * ImagePlusPlus("./2A_SLM.jpg"); impp.show();
	}*
     */
}
