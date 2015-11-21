package adviewer.gui;

import adviewer.image.EPICSImageStream;
import ij.IJ;
import ij.ImageJ;
import ij.gui.ImageCanvas;
import ij.gui.ImageLayout;

import java.awt.Image;
import java.awt.Panel;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import com.charliemouse.cambozola.shared.ImageChangeEvent;
import com.charliemouse.cambozola.shared.ImageChangeListener;

import adviewer.image.ImagePlusPlus;
import adviewer.image.ImageStream;
import adviewer.image.MJPGImageStream;
import adviewer.image.Stream;
import adviewer.util.CameraConfig;
import adviewer.util.LUTCollection;
import adviewer.util.Log;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * Container that holds an ImageCanvas -> ImagePlusPlus. Trying to get out of
 * using ImagePlus.show() to control the way the window is drawn (adding
 * components)
 *
 * @author root
 *
 */
public class ImagePanel extends JPanel implements FocusListener, MouseWheelListener, ImageChangeListener, ActionListener {

    //Fields
    volatile public ImagePlusPlus impp;
    public ImagePlusPlus impp2;
    public ImageCanvas ic;
    public boolean newCanvas;
    public ImageJ ij;
    public Image img;
    public byte[] imgBytes;
    public CameraConfig cam;
    public ImageStream streamer;
    public String streamType = "mjpg";
    public ADWindow win;
    public ADMainPanel mainPanel;
    public boolean isImageAquired = false;
    public Timer timer;
    public int TIME = 30;
    public static final String MJPG = "mjpg";
    public static final String EPICS = "epics";
    public boolean streamChanged = false;
    private LUTCollection luts;

    ImagePanel() {
        super();
    }

    ImagePanel(CameraConfig cam, ADWindow win) {
        super();
        this.cam = cam;

        this.ij = IJ.getInstance();

        this.win = win;
        this.mainPanel = win.adpanel;

        //try to start stream and get impp from stream
        // this.impp = null; //set impp to null
        Log.log("IMPP is still null .... ", win.debug);

        //try to start the stream ...
        this.streamer = startStream(cam.getConnectionType()); //this should set streamer
        this.win.setImageStream(streamer);

        Log.log("Stream started ....", win.debug);

        waitForFirstImage(); //block thread until image is aquired

        Log.log("image was aquired ....", win.debug);

        timer = new Timer(this.TIME, this);
        init();

    }

    public ImagePanel(ImagePlusPlus impp, ADWindow win) {
        this(impp, impp.getCanvas(), win);

    }

    public ImagePanel(ImagePlusPlus impp, ImageCanvas ic, ADWindow win) {
        super();
        this.impp = impp;
        this.ic = ic;
        this.win = win;

        init();

    }

    public void init() {
        this.ij = IJ.getInstance();

        //check to see if this a new canvas
        if (this.impp != null && this.streamer != null) {

            this.timer.start();
        }
        if (this.ic == null) {
            newCanvas = true;
            this.ic = new ImageCanvas(this.impp);

            //this.ic.update(this.ic.getGraphics());
            Log.log("New Canvas Created .... mag = " + this.ic.getMagnification(), win.debug);
        }

        if (this.ic.getMagnification() != 0.0) {
            impp.setTitle(this.impp.getTitle());
        }
        //boolean unlocked = this.impp.lockSilently();
        boolean changes = this.impp.changes;
        //impp.changes = false;

        //impp.changes = changes;
        setLayout(new ImageLayout(this.ic));
        add(this.ic);
        addFocusListener(this);
        //addWindowListener(this);
        //addWindowStateListener(this);
        addKeyListener(this.ij);
        setFocusTraversalKeysEnabled(false);

    }

    public void waitForFirstImage() {
        boolean isImage = false;

        while (!isImage) {     //blocks the main thread until we have aquired our first image

            isImage = this.isImageAquired;
            //System.out.println("isImage = "+ isImage);
            if (isImage) {
                try {
                    if (win != null) {
                        luts = win.luts;
                    }
                    if (cam.getConnectionType().equals(MJPG)) {
                        if (luts != null) {
                            this.impp = new ImagePlusPlus(cam.getName(), img, this.streamer, luts);
                        } else {
                            this.impp = new ImagePlusPlus(cam.getName(), img, this.streamer);
                        }
                    } else {

                        if (luts != null) {
                            this.impp = new ImagePlusPlus(cam.getName(),
                                    new ByteProcessor(streamer.imageWidth, streamer.imageHeight, streamer.getRawImage()),
                                    this.streamer,
                                    this.luts);
                        } else {

                            this.impp = new ImagePlusPlus(cam.getName(),
                                    new ByteProcessor(streamer.imageWidth, streamer.imageHeight, streamer.getRawImage()),
                                    this.streamer
                            );
                        }
                    }

                    this.impp.showMe();

                    //give reference to impp to the streamer and then 
                    //make sure impp knows what is updating it
                    streamer.impp = this.impp;
                    this.impp.setImageUpdater(streamer);

                    impp.setImagePanel(this);   //pass a reference to ImagePanel in ImagePlusPlus
                    streamer.setImagePanel(this); //pass an ImagePanel reference to the streamer

                    break;
                } catch (Exception e) {
                    if (win.debug) {
                        Log.log("Exception caught while creating IMPP ", win.debug);
                        e.printStackTrace();
                        System.exit(1);
                    }
                }
            } else {
                try {
                    //System.out.println("isImage = "+ isImage);

                    Thread.sleep(500); //sleep to give some time to get first image .... 
                } catch (InterruptedException ex) {
                    Log.log("Interupt occured upon waiting for image ...", win.debug);
                    System.exit(1);
                }
                continue;
            }

        }
    }

    //get type from cam config and then start stream based on type
    public ImageStream startStream(String type) {
        try {
            if (type.equals(MJPG)) {

                streamer = new MJPGImageStream(cam, win.debug);
                //win.addThread(streamer);

            } else if (type.equals(EPICS)) {
                streamer = new EPICSImageStream(cam, win.debug);
                // win.addThread(streamer);
            }

            if (streamer != null) {
                streamer.start();
                streamer.addImageChangeListener(this);
            } else {
                Log.log("Streamer was null ...", win.debug);

            }
        } catch (Exception e) {
            Log.log("Problem executing stream ...\n", win.debug);

            if (win.debug) {
                e.printStackTrace();
            }
            System.exit(1);
        }

        return streamer;
    }

    public void setMainPanel(ADMainPanel mainPanel) {
        this.mainPanel = mainPanel;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        // TODO Auto-generated method stub
    }

    @Override
    public void focusGained(FocusEvent arg0) {
    }

    @Override
    public void focusLost(FocusEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void imageChanged(ImageChangeEvent ie) {
        // TODO Auto-generated method stub
        //System.out.println("Image Changed....");
        //update(getGraphics());
        //getToolkit().sync();

        this.isImageAquired = true;
        //System.out.println("isImageAquired = "+this.isImageAquired);
        this.img = streamer.getCurrent();
        this.imgBytes = streamer.getRawImage();

        //streamer.numImageUpdates++;
        //Log.log("Image changed .... ", win.debug);
    }

    @Override
    public void actionPerformed(ActionEvent ae) {

        try{
            if (!streamer.isAlive() && img != null && imgBytes != null) {
                Log.log("timer is stopping .... ", win.debug);
                timer.stop();
                System.exit(1);
                return;
            }

            if (streamChanged) {
                changeStream();
            }

            if (this.impp != null && cam.getConnectionType().equals(MJPG) && !streamChanged) {
                this.impp = streamer.updateImpp(this.impp, new ImagePlusPlus(cam.getName(), img));
                doImageStuff();

            } else if (this.impp != null && cam.getConnectionType().equals(EPICS) && !streamChanged) {
                this.impp = streamer.updateImpp(this.impp, new ImagePlusPlus(cam.getName(), new ByteProcessor(streamer.imageWidth, streamer.imageHeight, streamer.getRawImage())));
                doImageStuff();

            }
        }
        catch(Exception e){
            Log.log(e.getMessage(), win.debug);
        }
        /**
         * else { Log.log("timer is stopping .... ", win.debug); timer.stop();
         * System.exit(1);
         *
         * }*
         */
    }

    public void doImageStuff() {

        //Log.log("Action preformed", win.debug);
        this.streamer.setImageTitle();

        this.win.impp = this.impp;
        this.isImageAquired = false;

        streamer.numImageUpdates++;
        //streamer.doFpsCalc();

        //this.impp.setFPSText(streamer.fpsFormatted);
    }

    public void changeStream() {
        try {
            //try to change data stream
            timer.stop();

            streamer.kill();
            streamer = startStream(cam.getConnectionType());
            this.win.setImageStream(streamer);
            //waitForFirstImage(); //blocks this thread and waits for an image
            impp.imageUpdater = streamer; // sets immp streamer to new value

            timer.start();

            Thread.sleep(TIME + 100);
            streamChanged = false; // always set back to false
            Log.log(" stream changed back to = " + streamChanged, win.debug);
        } catch (Exception e) {
            Log.log("Problem with setting new stream", win.debug);
            if (win.debug) {
                e.printStackTrace();
            }
        }
    }
}
