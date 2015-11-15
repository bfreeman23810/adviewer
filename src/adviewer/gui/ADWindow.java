package adviewer.gui;

import adviewer.util.Config;
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
 * adviewer.gui.ADWindow.java
 * Top level Frame. This will hold all gui components. Extending ImageJ's
 * ImageWindow Class, to still exploit ImageJ like functionality. The Main 
 * container is ADMainPanel.java/
 * 
 * To start this application first set camera parameters in cam.json
 * (Camera types can be EPICS areaDetector or MJPEG streams)
 * Then type, "run -h" for list of commands
 * Turn debug on by typing -debug
 * 
 * Usage: run -id MYCAM -debug
 * 
 * For information on ImageJ: http://imagej.nih.gov/ij/
 * For information on areaDector: http://cars9.uchicago.edu/software/epics/areaDetector.html
 *
 * @author Brian Freeman
 * 
 *
 */
public class ADWindow extends ImageWindow implements WindowListener {

    public ImagePlusPlus impp;      //ImagePlus Extension from ImageJ
    public ImageJ ij;               //IJ instance
    public ADWindow previousWindow;
    public ADMainPanel adpanel;    //Main Frame Container
    public CameraConfig cam;        //current camera
    public CameraCollection cams;   //collection of cams
    public LUTCollection luts;      //luts
    public ArrayList<Thread> threads;   //thread tracking
    public ImageStream imageUpdater;    //if live stream, keep reference to what updates this
    private ImageStream stream;        

    //These parameters are for commadline options using JCommander  
    // see: http://jcommander.org/
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
    private Config config;

    /**
     * Constructor takes in the Window title and the command line args
     * @param title
     * @param args 
     */
    public ADWindow(String title, String[] args) {
        super(title);
        BorderLayout layout = new BorderLayout(3,3);
        this.setLayout(layout);
        
        //JCommander object to parse agrs
        jcomm = new JCommander(this, args);
        jcomm.setProgramName("adviewer");

        Log.log("my working dirictory .... " + currDir, debug);
        
        config = new Config(this.currDir, debug);
        
        cams = new CameraCollection(config.getCamConfig(), debug);
        luts = new LUTCollection(config.getLutPath(), debug);

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

            this.impp = new ImagePlusPlus(imagePath , luts);
            this.imp = impp;
            this.adpanel = new ADMainPanel(impp, this, guiBuildNumber);
            this.ic = adpanel.ic;
            if(config!=null) this.adpanel.setIconPath(config.getIconPath()); //set the icon path
            
            if (cams != null) {
                this.adpanel.setCams(cams); //set cameras
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
        config = new Config(this.currDir, debug);
        
        if (impp != null) {
            //if impp was defined then we have some static image we are try to look at
            this.adpanel = new ADMainPanel(impp, this, guiBuildNumber);
            if(config!=null) this.adpanel.setIconPath(config.getIconPath()); //set the icon path
            
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
        if(config!=null) this.adpanel.setIconPath(config.getIconPath()); //set the icon path
         
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

   

}
