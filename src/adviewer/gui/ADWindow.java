package adviewer.gui;

import ij.IJ;
import ij.ImageJ;
import ij.WindowManager;
import ij.gui.ImageWindow;
import ij.macro.Interpreter;

import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import adviewer.image.ImagePlusPlus;
import adviewer.image.ImageStream;
import adviewer.util.CameraCollection;
import adviewer.util.CameraConfig;
import adviewer.util.LUTCollection;
import adviewer.util.Log;
import adviewer.util.SystemCommand;
import com.beust.jcommander.JCommander;
import static java.lang.System.exit;
import java.util.ArrayList;

import com.beust.jcommander.Parameter;
import java.awt.BorderLayout;

/**
 * Top level Frame. This will hold all gui components. Extending ImageJ's
 * ImageWindow Class, to still exploit ImageJ like functionality
 *
 * @author root
 *
 */
public class ADWindow extends ImageWindow implements WindowListener {

   public static String ADCONFIG = "/root/workspace/java/adviewer/config/adviewer.config";
    //public static String ADCONFIG = "/cs/dvlhome/apps/a/adViewer/dvl_2-0/src/adviewer/config/adviewer.config";

    public ImagePlusPlus impp;      //ImagePlus Extension from ImageJ
    public ImageJ ij;               //IJ instance
    public ADWindow previousWindow;
    public ADMainPanel adpanel;    //Holds the ImagePlusPlus
    public CameraConfig cam;
    public CameraCollection cams;
    public LUTCollection luts;
    public ArrayList<Thread> threads;
    public ImageStream imageUpdater;
    private ImageStream stream;

    @Parameter(names = {"-debug"}, description = "Set Debugging ")
    public boolean debug = false;
    //@Parameter()
    public boolean isImage;

    @Parameter(names = {"-id"}, description = "Start by Camera ID")
    public String id;

    @Parameter(names = {"-i"}, description = "image path")
    public String imagePath;

    @Parameter(names = {"-pv"}, description = "PV Prefix for epics camera")
    public String pv;

    @Parameter(names = {"-help", "-h"}, description = "Print usage", help = true)
    public boolean help = false;

    @Parameter(names = {"-d"}, description = " working directory ", hidden = true)
    public String currDir;

    @Parameter(names = {"-g"}, description = "gui build level ... 0,1,2 ", required = false)
    public int guiBuildNumber = 1;

    public JCommander jcomm;

    public ADWindow(String title, String[] args) {
        super(title);
        BorderLayout layout = new BorderLayout(3,3);
        this.setLayout(layout);
        
        jcomm = new JCommander(this, args);
        jcomm.setProgramName("adviewer");

        Log.log("my working dirictory .... " + currDir, debug);

        cams = new CameraCollection(ADCONFIG, false);
        luts = new LUTCollection(ADCONFIG, true);

        if (help == true) {
            this.usage();
        } else if (id == null && imagePath == null && pv == null) {

            this.usage();
            //System.exit(1);
        } else if (this.id != null) {

            this.init(cams.map.get(id));

        } else if (this.imagePath != null) {

            if (!imagePath.contains("/") || imagePath.contains("./")) {
                imagePath = currDir + "/" + imagePath;
            }

            Log.log("Path to image .... " + imagePath, debug);

            this.impp = new ImagePlusPlus(imagePath);
            this.imp = impp;
            this.adpanel = new ADMainPanel(impp, this, guiBuildNumber);
            this.ic = adpanel.ic;
            if (cams != null) {
                this.adpanel.setCams(cams);
            } else {
                Log.log("Cams is null ", debug);
            }

            this.setTitle(impp.getTitle());
            init(impp); //must be called here

            //at a minumum this window will have an ADMainPanel, so adding here
            // this.add(adpanel);
            Log.log("Panel added .... ", debug);

        } else {
            this.usage();

        }

        //init();
    }

    public ADWindow(ImagePlusPlus impp) {
        super(impp.getTitle());

        if (impp != null) {
            //if impp was defined then we have some static image we are try to look at
            this.adpanel = new ADMainPanel(impp, this, guiBuildNumber);
            if (cams != null) {
                this.adpanel.setCams(cams);
            } else {
                Log.log("Cams is null ", debug);
            }

            this.ic = adpanel.ic;
            this.imp = impp;
            this.impp = impp;
        }

        init(impp); //must be called here

        //at a minumum this window will have an ImagePanel, so adding here
        // this.add(adpanel);
    }

    public void init(CameraConfig cam) {

        this.cam = cam;

        this.adpanel = new ADMainPanel(cam, this, guiBuildNumber);
        if (cams != null) {
            this.adpanel.setCams(cams);
        } else {
            Log.log("Cams is null ", debug);
        }

        if (adpanel.impp != null) {
            this.impp = adpanel.impp;
            this.imp = adpanel.impp;
            this.ic = adpanel.ic;
        }

        init(impp); //must be called here
        Log.log("Done with init() .... ", debug);

        //at a minumum this window will have an ADMainPanel, so adding here
        this.add(this.adpanel);

    }

    public void addThread(Thread t) {
        threads.add(t);
    }

    void setImageStream(ImageStream stream) {
        this.stream = stream;
    }

    /**
     * init is used to initialize common params
     */
    public void init(ImagePlusPlus impp) {

        super.ij = IJ.getInstance();

        threads = new ArrayList<Thread>();

        //super.ij=ij;
        //this.imagePanel = new ImagePanel(impp);
        //adpanel.win = this; 
        this.closed = false;

        // if impp is still null here then we somwthing went wrong
        if (this.impp == null) {
            Log.log("impp is null in ADWindow .... exiting", debug);
            exit(1);
        } else {
            this.impp.setWindow(this);
            this.impp.showMe();
        }

        //add listeners
        previousWindow = (ADWindow) this.impp.getWindow();

        addFocusListener(this);
        addWindowListener(this);
        addWindowStateListener(this);
        addKeyListener(ij);
        setFocusTraversalKeysEnabled(false);
        //if (!(this instanceof StackWindow))
        addMouseWheelListener(this);
        setResizable(true);

        WindowManager.addWindow(this);
        WindowManager.setTempCurrentImage(impp);
        Interpreter.addBatchModeImage(impp);

        setForeground(Color.black);

        if (IJ.isLinux()) {
            setBackground(ImageJ.backgroundColor);
        } else {
            setBackground(Color.white);
        }

        this.add(adpanel);

    }

    public static void main(String[] args) {

        //ImagePlusPlus impp = new ImagePlusPlus("./2A_SLM_bw.jpg");
        //CameraConfig cam = new CameraConfig("me" , "stream" , "http://localhost:8081/stream.mjpg" , "mjpg");
        //CameraConfig cam = new CameraConfig("me" , "stream" , "http://subopsl08:8081/stream.mjpg" , "mjpg");
        // CameraConfig cam = new CameraConfig("bc1" , "Blue Cherry Ch 1" , "https://opsdvr:7001/media/mjpeg.php?multipart=false&id=4" , "mjpg");
        // CameraConfig cam = new CameraConfig("slminj" , "SLM INJ" , "http://opscam1:8081/SLMINJMJPG1.mjpg" , "mjpg");
        //CameraConfig cam = new CameraConfig("slm1a" , "SLM 1A" , "http://opscam1:8082/SLM1AMJPG1.mjpg" , "mjpg");
        //ADWindow adwindow = new ADWindow( impp );
        ADWindow adwindow = new ADWindow("New", args);

        Log.log("Here .... in main ", adwindow.debug);

        adwindow.pack();
        adwindow.setVisible(true);
    }

    @Override
    public void windowActivated(WindowEvent e) {
        // TODO Auto-generated method stub
        super.windowActivated(e);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // TODO Auto-generated method stub
        super.windowClosed(e);
    }

    @Override
    public void windowClosing(WindowEvent e) {
        // TODO Auto-generated method stub
        try {

            //for registered threads and imagestream, close and stop threads
            for (Thread t : threads) {
                t.stop();
            }

            if (stream != null) {
                stream.kill();
                stream.stop();
            }

            if (closed) {
                System.out.println("window is closed ..... returning from WindowClosing Event");
                return;
            }

            if (ij != null) {
                ij.quit();
                ij.exitWhenQuitting(true);
                ij.dispose();
                System.out.println("IJ is exiting .... ");
            }

            super.windowClosing(e);
            super.setVisible(false);

            if (debug) {
                System.out.println("Closing ..... 'closed' = " + this.closed);
            }
            System.exit(0);

        } catch (Exception ex) {
            if (debug) {
                System.out.println("Caught exception upon closing window");
            }
            System.exit(1);
        }
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // TODO Auto-generated method stub
        super.windowDeactivated(e);
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // TODO Auto-generated method stub
        super.windowDeiconified(e);
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // TODO Auto-generated method stub
        super.windowIconified(e);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // TODO Auto-generated method stub
        super.windowOpened(e);
    }

    public void usage() {
        String s = "";

        jcomm.usage();
        System.out.println("Must use -i, -id, or -pv to declare an input....");
        System.out.println("Valid IDs are : \n" + cams.printCamListByID());
        System.out.println("... or use -i <Valid Picture File>");
        System.out.println("... or use -pv <PV PREFIX>");
        System.exit(0);
    }

    public void parseCommandLineArgs(String[] args) {

    }

}
