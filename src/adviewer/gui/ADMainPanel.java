package adviewer.gui;

import adviewer.plot.PlotPlus;
import adviewer.plot.PlotPanel;
import ij.gui.ImageCanvas;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JPanel;

import adviewer.image.ImagePlusPlus;
import adviewer.util.CameraCollection;
import adviewer.util.CameraConfig;
import adviewer.util.Helpers;
import adviewer.util.Log;
import adviewer.util.RunSystemCommand;
import adviewer.util.SwingLink;
import adviewer.util.SystemCommand;
import com.charliemouse.cambozola.Viewer;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.Plot;
import ij.gui.PlotCanvas;
import ij.gui.Roi;
import ij.gui.TextRoi;
import ij.gui.Toolbar;
import ij.io.FileSaver;
import ij.plugin.LutLoader;
import ij.process.LUT;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Label;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.IndexColorModel;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * ADMainPanel.java The main container for all components in an ADWindow Frame.
 *
 *
 * @author Brian Freeman
 *
 */
public class ADMainPanel extends JPanel implements ActionListener {

    //list of all components, thinking that all components will be available. and the ability to add components 
    //at runtime could be given
    public HashMap<String, Component> componetList;
    public ImagePlusPlus impp;
    public ImageCanvas ic;
    public CameraConfig cam;
    public CameraCollection cams;
    public ImagePanel imagePanel;
    public ADWindow win;
    public BorderLayout layout;
    // build levels - tells the main panel what components to build 
    public static int JUSTVIEWER = 0;
    public static int VIEWERWSTATUS = 1;
    public static int FULL = 2;
    public int guiBuildNUmber;
    //public Componets
    public Panel statusbar1;
    public JLabel fps;
    public JLabel pixelInspector;
    public JLabel messageLabel;
    private Border raisedetched;
    private Border blackline;
    private Border loweredetched;
    private Border raisedbevel;
    private Border loweredbevel;
    private Border empty;
    private TitledBorder title;
    private Panel south;
    private JLabel label;
    private JComboBox drop;
    private Panel northPanel;
    private Panel eastPanel;
    private Panel southPanel;
    private Panel dropPanel;
    private Legend legend;
    private IndexColorModel cm;
    private LUT lut;
    private LUT originalLut;
    public boolean isEastShowing;
    public boolean isXPlotShowing = false;
    public boolean isYPlotShowing = false;
    public String pathToIcons;
    private int guiBuildNumber;
    private FileSaver saver;
    public String savePath;
    public String fileName;
    public int seqCount = 1;
    public boolean saveAsSeq = false;
    private Panel xPlotPanel;
    private JPanel imagePanelContainer;
    private PlotPlus xp;
    private PlotPanel xpp;
    private Panel yPlotPanel;
    private PlotPlus yp;
    private PlotPanel ypp;
    public JLabel minXVal;
    public JLabel maxXVal;
    public JLabel centroidXVal;
    public JLabel rmsXVal;
    public JLabel twoRmsXVal;
    public JLabel fwhmXVal;
    public JLabel minYVal;
    public JLabel maxYVal;
    public JLabel centroidYVal;
    public JLabel rmsYVal;
    public JLabel twoRmsYVal;
    public JLabel fwhmYVal;
    public JLabel unitsVal;
    public boolean isSouthShowing;
    public GridBagConstraints gc;

    public static final String BC1 = "BC1";
    public static final String BC2 = "BC2";
    public static final String BC3 = "BC3";
    public static final String BC4 = "BC4";
    public static final String INC = "INC_CAM";
    private JComboBox<String> camChooser;
    private JLabel camChooserLabel;

    public ImagePlusPlus savedImpp;
    public JComboBox<String> lutChoiceBox;
    public JButton playButton;
    public JButton pauseButton;
    public JButton saveBackGroundButton;
    public JButton backGroundSubOnButton;
    public JButton edgesButton;
    public JLabel statusLight;
    
    /**
     * This version of the constructor takes in a Camera Object, window, and int
     *
     * @param cam The CameraConfig Object
     * @param win ADWindow where will draw the panel
     * @param guiBuildNumber int gui build number 0,1, or 2
     */
    public ADMainPanel(CameraConfig cam, ADWindow win, int guiBuildNumber) {
        super();
        this.win = win;
        this.cam = cam;
        init(guiBuildNumber);

    }

    /**
     * This version of the constructor is for building static images
     *
     * @param impp - ImagePlusPlus Object
     * @param win - ADWindow where we draw this panel
     * @param guiBuildNumber - 0, 1, 2
     */
    public ADMainPanel(ImagePlusPlus impp, ADWindow win, int guiBuildNumber) {
        super();
        this.win = win;
        this.impp = impp;
        //this.win.impp = impp;
        init(guiBuildNumber);

    }

    /**
     * Set CameraCollection
     *
     * @param cams
     */
    public void setCams(CameraCollection cams) {
        this.cams = cams;
    }

    /**
     * initialize common variables
     *
     * @param guiBuildNumber - 0, 1, or 2 see definitions in ADWindow
     */
    public void init(int guiBuildNumber) {
        layout = new BorderLayout(3, 3);
        this.setLayout(layout);
        layout.setHgap(3);

        this.guiBuildNumber = guiBuildNumber; //set gui build number
        this.pathToIcons = this.win.config.getIconPath(); //set path to icons to locarion in config file, defined in ADWindow

        gc = new GridBagConstraints(); //global gridbag constraints

        if (impp != null) {
            imagePanel = new ImagePanel(impp, this.win);    //set image panel using impp, need this for static images
        } else {

            Log.log("No ImagePlusPlus provided ... so trying stream", win.debug);

            if (cam == null) { //cam should be passed in through the constructor
                String ids = "";
                if (this.win.cams != null) {   //build list of ids to show user
                    for (CameraConfig c : this.win.cams.cameras) {
                        ids += c.getId() + "\n";
                    }
                }
                Log.err("Please provide a Camera Config\nValid ids are:\n" + ids, win.debug); //show user list of valid ids

                return;
            } else {
                imagePanel = new ImagePanel(cam, this.win); //this statement will block until it collects the first image
            }
        }

        if (this.win != null) { //if I still have a windoe to draw in...
            this.win.impp = imagePanel.impp;    //tell ADWindow which impp obj
            this.impp = this.win.impp;          // ensure my impp is the same
            this.ic = imagePanel.ic;            // set my imageCanvas obj to the same as imagePanel's

            this.cams = this.win.cams;          //set my camera collection to the same as ADWindow's
            this.imagePanel.setMainPanel(this); //set a reference in ImagePanel to this MainPanel 

            this.add(createImagePanelContainer(), BorderLayout.CENTER); //create the container that will hold the imagePAnel

            addComponents(guiBuildNumber); //now add components
        }

    }

    /**
     * Container for ImagePanel
     *
     * @return
     */
    public Component createImagePanelContainer() {
        imagePanelContainer = new JPanel(new GridBagLayout());
        //gc.weightx = gc.weighty = 1.0;
        
        gc.gridx = 1;
        gc.gridy = 0;

        if (imagePanel != null) {
            imagePanelContainer.add(imagePanel, gc);
        }

        return imagePanelContainer;
    }

    public void addComponents(int guiBuildNumber) {
        //global borders to use
        blackline = BorderFactory.createLineBorder(Color.black);
        raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        raisedbevel = BorderFactory.createRaisedBevelBorder();
        loweredbevel = BorderFactory.createLoweredBevelBorder();
        empty = BorderFactory.createEmptyBorder();

        title = BorderFactory.createTitledBorder("title");

        try {
            if (guiBuildNumber == JUSTVIEWER) { //gui level 0, just build ImagePanel
                return;
            }
            if (guiBuildNumber > JUSTVIEWER) { //if greater that JUSTVIEWER, i.e. 1 or 2
                isEastShowing = false;
                this.add(createNorth(), BorderLayout.NORTH);    //build north  panel
                if (imagePanel != null) {
                    southPanel = (Panel) createSouth();
                   
                    if (imagePanel.streamer != null && imagePanel.streamer.isCEBAFBC) { //This is a site specific gui component

                        //Site specific component - if the ids are seen as certain channels it will build a south panel
                        if (isCEBAFBCandMakeMonitor(cam)) {
                            this.showSouth();
                        }
                    }
                }

            }
            if (guiBuildNumber > VIEWERWSTATUS) { //if gui level is 2, then fully expand the gui
                isEastShowing = true;
                eastPanel = (Panel) createEast();
                this.add(eastPanel, BorderLayout.EAST);

            }

            //showLegend();
        } catch (Exception e) {
            if (win.debug) {
                e.printStackTrace();
            }
        }
    }

    public Component createStatusBar1() throws IOException {
        statusbar1 = new Panel(new BorderLayout());

        JPanel p = new JPanel(new FlowLayout(2));

        fps = makeReadback(" 0.00 fps ", new Dimension(75, 20));

        pixelInspector = makeReadback("Cursor Position and Pixel Value", new Dimension(200, 20));

        ImageIcon right = createImageIcon(pathToIcons + "plus_black_small.png", "Button1");
        ImageIcon left = createImageIcon(pathToIcons + "minus_black_small.png", "Button1T");

        //Log.log(pathToIcons + "plus_black_small.png" , true);
        ButtonIcon eastButton = null;

        if (this.guiBuildNumber > VIEWERWSTATUS) {
            eastButton = new ButtonIcon(left, right);

        } else {
            eastButton = new ButtonIcon(right, left);

        }

        if (eastButton != null) {
            eastButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {

                    toggleEastPanel();
                }
            });
            eastButton.setPreferredSize(new Dimension(20, 20));
            eastButton.setToolTipText("Click to show/ hide expanded Controls");
            // buttons.add(eastButton, BorderLayout.EAST);
        }

        JPanel p1 = new JPanel(new FlowLayout(2));
        JPanel p2 = new JPanel(new FlowLayout(5));

        Panel camPanel = (Panel) createCamDropDown(); //creates panel and defines some global vars

        p1.add(camChooserLabel);    //defined in createCamDropDown()
        p1.add(camChooser);         //defined in createCamDropDown()
        p1.add(pixelInspector);

        p2.add(fps);
        p2.add(eastButton);

        statusbar1.add(p1, BorderLayout.WEST);
        statusbar1.add(p2, BorderLayout.EAST);

        return statusbar1;
    }

    public Component createNorth() throws IOException {
        northPanel = new Panel(new BorderLayout());

        northPanel.add(createStatusBar1(), BorderLayout.NORTH);
        //northPanel.add(createCamDropDown() , BorderLayout.NORTH);

        return northPanel;
    }

    public Component createPlotPanel() {

        Panel plots = new Panel(new GridBagLayout());

        JPanel p = new JPanel(new GridBagLayout());

        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx = 0;
        gc.gridy = 1;
        p.add(createXPlotsButtonPanel(), gc);

        gc.gridx = 0;
        gc.gridy = 2;

        p.add(createYPlotsButtonPanel(), gc);

        gc.gridx = 0;
        gc.gridy = 3;

        p.add(createUnitsPanel(), gc);

        gc.gridx = 0;
        gc.gridy = 4;

        JPanel p2 = new JPanel(new FlowLayout(5));
        p2.add(createXFitsPanel());
        p2.add(createYFitsPanel());
        p.add(p2, gc);

        gc.gridx = 0;
        gc.gridy = 0;
        plots.add(p, gc);

        return plots;

    }

    /**
     * Returns an ImageIcon, or null if the path was invalid.
     */
    protected ImageIcon createImageIcon(String path, String description) {
        //java.net.URL imgURL = getClass().getResource(path);
        String imgURL = path;
        try {
            ImageIcon ico = new ImageIcon(path, description);
            //Log.log("Image Icon created .... " + description, win.debug);
            return ico;
        } catch (Exception e) {
            Log.log("Couldn't find file: " + path, win.debug);
            if (win.debug) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public Component createEast() {

        eastPanel = new Panel();
        

        eastPanel.setFocusable(false);

        JTabbedPane tabs = new JTabbedPane();

        tabs.add(createImagePanel(), "Image");
        tabs.add(createToolsPanel(), "Tools");
        tabs.add(createPlotPanel(), "Plots");
        tabs.add(createSavePanel(), "Save");
        tabs.add(createInfoPanel(), "Info");

        tabs.setSelectedIndex(0);
        eastPanel.add(tabs);

        return eastPanel;
    }

    public Component createXPlotPanel() {
        xPlotPanel = new Panel();
        
        //JTabbedPane tabs = new JTabbedPane();

        //tabs.setPreferredSize(new Dimension( impp.getWidth() , impp.getHeight()));
        //tabs.add(createXPlot(), "xplot");

        xPlotPanel.add(createXPlot());
        
        return xPlotPanel;
    }

    public Component createYPlotPanel() {
        yPlotPanel = new Panel();

        //JTabbedPane tabs = new JTabbedPane();

        //tabs.setPreferredSize(new Dimension( impp.getWidth() , impp.getHeight()));
        //tabs.add(createYPlot(), "yplot");

        yPlotPanel.add(createYPlot());
       
        
        return yPlotPanel;
    }

    public Component createXPlot() {
        JPanel panel = new JPanel();

        boolean isY = false;
        boolean showFit = false;
        boolean isAveraged = true;

        xp = Helpers.getPlotPlus(impp, isY, showFit, isAveraged, win.debug);
        xpp = new PlotPanel(xp, impp, isY, win.debug);
        xpp.setMainPanel(this); //set a refernce to this panel in PlotPanel

        //PlotCanvas pc = new PlotCanvas(impp);
        //pc.setPlot(xp);
       
        panel.add(xpp);

        return panel;
    }

    public Component createYPlot() {
        JPanel panel = new JPanel();

        boolean isY = true;
        boolean showFit = false;
         boolean isAveraged = true;

        yp = Helpers.getPlotPlus(impp, isY, showFit, isAveraged, win.debug);
        ypp = new PlotPanel(yp, impp, isY, win.debug);
        ypp.setMainPanel(this); //set a refernce to this panel in PlotPanel

        
        
        panel.add(ypp);

        return panel;
    }

    public Component createImagePanel() {
        //JPanel panel = new JPanel(new GridBagLayout());
        // panel.setPreferredSize(new Dimension(200 , 30) );
        GridBagConstraints gc = new GridBagConstraints();
        
        JPanel p = new JPanel(new GridBagLayout());
       
        
        
        gc.weightx = 1.0;
        gc.weighty = 1.0;
        //gc.fill = GridBagConstraints.HORIZONTAL;

       

        gc.gridx = 0;
        gc.gridy = 0;
        //gc.anchor = GridBagConstraints.WEST;

        p.add(createBackGroundSubtractPanel(), gc);

        gc.gridx = 0;
        gc.gridy = 1;
        //gc.anchor = GridBagConstraints.WEST;

        p.add(createLutDropDownPanel(), gc);

        gc.gridx = 0;
        gc.gridy = 2;
       // gc.anchor = GridBagConstraints.WEST;

        p.add(createGammaAdjust(), gc);

        gc.gridx = 0;
        gc.gridy = 3;
        //gc.anchor = GridBagConstraints.WEST;

        p.add(createBrightnessAdjust(), gc);

       // gc.gridx = 0;
        //gc.gridy = 0;
        
        return p;
    }

    
    private  void open(URI uri) {
    if (Desktop.isDesktopSupported()) {
      try {
        //Desktop.getDesktop().browse(uri);
          SystemCommand.exec("firefox "+uri + " &");
        Log.log("trying to open " + uri.toString() , win.debug);
      } catch (Exception e) { 
          Log.log("Error in open " + e.getMessage(), win.debug);
          e.printStackTrace();
      }
    } else { Log.log("Unsupported desktop " , win.debug); }
  }
    
    public Component createTestButton() {
        Panel panel = new Panel();

        JPanel p = new JPanel();

        JButton b = new JButton("Test Out Things");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    //toggleXPlotPanel();
                    //Roi roi = new Roi(70, 70, 120, 120);
                    // Line line = new Line(impp.getWidth() / 2, impp.getHeight() - 1, impp.getWidth() / 2, 0);
                    // TextRoi text = new TextRoi(20, 20, "test");
                    //impp.setRoi(text);
                    //impp.saveRoi();
                    //impp.setRoi(roi);
                    //Overlay ov = new Overlay(roi);
                    //impp.setOverlay(ov);
                    //impp.updateAndRepaintWindow();
                    //imagePanel.ic.;
                   // win.pack();
                    URI uri = new URI("https://github.com/bfreeman23810/adviewer/wiki/GUI-Help");
                    open(uri);
                } catch (URISyntaxException ex) {
                   Log.log("Error in open .... " + ex.getMessage() , win.debug);
                }
            }
        });

        p.add(b);
        title = BorderFactory.createTitledBorder("Test");
        p.setBorder(title);
        panel.add(p);

        panel.setFocusable(false);
        return panel;
    }

    public Component createXPlotsButtonPanel() {
        Panel panel = new Panel();

        JPanel p = new JPanel(new FlowLayout(2));

        final JButton b = new JButton("X PLOT");
        JButton b1 = new JButton("UPDATE");

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                toggleXPlotPanel();
                if (isXPlotShowing) {
                    b.setBackground(Color.GREEN);
                } else {
                    b.setBackground(null);
                }
            }
        });

        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                if (xpp != null) {
                    xpp.restartLive();
                }

            }
        });

        p.add(b);
       // p.add(b1);

        title = BorderFactory.createTitledBorder("Horizontal Plots");
        p.setBorder(title);
        panel.add(p);

        p = new JPanel(new FlowLayout(2));

        final JCheckBox c = new JCheckBox();

        c.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {

                if (c.isSelected()) {
                    if (xpp != null) {
                        xpp.showFit = true;
                    }
                } else {
                    if (xpp != null) {
                        xpp.showFit = false;
                    }
                }

            }
        });
        p.add(new JLabel("     "));
        p.add(c);
        p.add(new JLabel("     "));
        title = BorderFactory.createTitledBorder("Show Fits");
        p.setBorder(title);

        panel.add(p);

        panel.setFocusable(false);
        return panel;
    }

    public Component createYPlotsButtonPanel() {
        Panel panel = new Panel();

        JPanel p = new JPanel(new FlowLayout(2));

        final JButton b = new JButton("Y PLOT");
        JButton b1 = new JButton("UPDATE");

        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                toggleYPlotPanel();
                if (isYPlotShowing) {
                    b.setBackground(Color.GREEN);
                } else {
                    b.setBackground(null);
                }
            }
        });

        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                if (ypp != null) {
                    ypp.restartLive();
                }

            }
        });

        p.add(b);
        //p.add(b1);

        title = BorderFactory.createTitledBorder("Vertical Plots");
        p.setBorder(title);
        panel.add(p);

        p = new JPanel(new FlowLayout(2));

        final JCheckBox c = new JCheckBox();

        c.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {

                if (c.isSelected()) {
                    if (ypp != null) {
                        ypp.showFit = true;
                    }
                } else {
                    if (ypp != null) {
                        ypp.showFit = false;
                    }
                }

            }
        });
        p.add(new JLabel("     "));
        p.add(c);
        p.add(new JLabel("     "));
        title = BorderFactory.createTitledBorder("Show Fits");
        p.setBorder(title);

        panel.add(p);

        panel.setFocusable(false);
        return panel;
    }

    public Component createUnitsPanel() {
        Panel panel = new Panel();

        JPanel p = new JPanel(new FlowLayout(2));

        JLabel unitsLabel = new JLabel("  Units :   ");
        unitsVal = makeReadback("  " + this.impp.getCalibration().getUnits(), new Dimension(60, 20));

        p.add(unitsLabel);
        p.add(unitsVal);

        title = BorderFactory.createTitledBorder("Units");
        p.setBorder(title);
        panel.add(p);
       // panel.add(createTurnOffAverage()); //come back to this.... errors on ROI selection and profiles incoorect
        return panel;
    }

    public Component createXFitsPanel() {

        Panel panel = new Panel();

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        //gc.weightx = gc.weighty = 1.0;
        gc.insets = new Insets(2,2,2,2);
        
        String sigma = "  \u03A3";
        Dimension d = new Dimension(80, 20);

        JLabel min = new JLabel("  Y Min:");
        min.setPreferredSize(d);

        minXVal = makeReadback("  min", d);

        JLabel max = new JLabel("  Y Max:");
        max.setPreferredSize(d);
        max.setAlignmentX(JLabel.EAST);

        maxXVal = makeReadback("  max", d);

        JLabel centroid = new JLabel("  Centroid:");
        centroid.setPreferredSize(d);
        centroid.setAlignmentX(JLabel.EAST);

        centroidXVal = makeReadback("  centroid", d);

        JLabel rms = new JLabel(sigma.toLowerCase() + ":        ");
        rms.setPreferredSize(d);
        rms.setAlignmentX(JLabel.EAST);

        rmsXVal = makeReadback("  rms", d);

        JLabel twoRms = new JLabel("  2*" + sigma.toLowerCase() + ":");
        twoRms.setPreferredSize(d);
        twoRms.setAlignmentX(JLabel.EAST);

        twoRmsXVal = makeReadback("  2*rms", d);

        JLabel fwhmx = new JLabel("  FWHMx:");
        fwhmx.setPreferredSize(d);
        fwhmx.setAlignmentX(JLabel.EAST);

        fwhmXVal = makeReadback("  FWHMx", d);

        gc.gridx = 0;
        gc.gridy = 0;

        p.add(centroid, gc);

        gc.gridx = 0;
        gc.gridy = 1;

        p.add(rms, gc);

        gc.gridx = 0;
        gc.gridy = 2;

        p.add(twoRms, gc);

        gc.gridx = 0;
        gc.gridy = 3;

        p.add(fwhmx, gc);

        gc.gridx = 0;
        gc.gridy = 4;

        p.add(min, gc);

        gc.gridx = 0;
        gc.gridy = 5;

        p.add(max, gc);

        gc.gridx = 1;
        gc.gridy = 0;

        p.add(centroidXVal, gc);

        gc.gridx = 1;
        gc.gridy = 1;

        p.add(rmsXVal, gc);

        gc.gridx = 1;
        gc.gridy = 2;

        p.add(twoRmsXVal, gc);

        gc.gridx = 1;
        gc.gridy = 3;

        p.add(fwhmXVal, gc);

        gc.gridx = 1;
        gc.gridy = 4;

        p.add(minXVal, gc);

        gc.gridx = 1;
        gc.gridy = 5;

        p.add(maxXVal, gc);

        title = BorderFactory.createTitledBorder("X Fit Data");
        p.setBorder(title);

        panel.add(p);
        return panel;

    }

    public Component createYFitsPanel() {

        Panel panel = new Panel();

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(2,2,2,2);
        
        String sigma = "  \u03A3";
        Dimension d = new Dimension(80, 20);

        JLabel min = new JLabel("  Min:");
        min.setPreferredSize(d);
        min.setAlignmentX(JLabel.EAST);

        minYVal = makeReadback("  min", d);

        JLabel max = new JLabel("  Max:");
        max.setPreferredSize(d);
        maxYVal = makeReadback("  max", d);

        JLabel centroid = new JLabel("  Centroid:");
        centroid.setPreferredSize(d);
        centroidYVal = makeReadback("  centroid", d);

        JLabel rms = new JLabel(sigma.toLowerCase() + ":");
        rms.setPreferredSize(d);
        rmsYVal = makeReadback("  rms", d);

        JLabel twoRms = new JLabel("  2*" + sigma.toLowerCase() + ":");
        twoRms.setPreferredSize(d);
        twoRmsYVal = makeReadback("  2*rms", d);

        JLabel fwhmy = new JLabel("  FWHMy:");
        fwhmy.setPreferredSize(d);
        fwhmYVal = makeReadback("  FWHMy", d);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.EAST;

        p.add(centroid, gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.anchor = GridBagConstraints.EAST;

        p.add(rms, gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.anchor = GridBagConstraints.EAST;

        p.add(twoRms, gc);

        gc.gridx = 0;
        gc.gridy = 3;
        gc.anchor = GridBagConstraints.EAST;

        p.add(fwhmy, gc);

        gc.gridx = 0;
        gc.gridy = 4;
        gc.anchor = GridBagConstraints.EAST;

        p.add(min, gc);

        gc.gridx = 0;
        gc.gridy = 5;
        gc.anchor = GridBagConstraints.EAST;

        p.add(max, gc);

        gc.gridx = 1;
        gc.gridy = 0;

        p.add(centroidYVal, gc);

        gc.gridx = 1;
        gc.gridy = 1;

        p.add(rmsYVal, gc);

        gc.gridx = 1;
        gc.gridy = 2;

        p.add(twoRmsYVal, gc);

        gc.gridx = 1;
        gc.gridy = 3;

        p.add(fwhmYVal, gc);

        gc.gridx = 1;
        gc.gridy = 4;

        p.add(minYVal, gc);

        gc.gridx = 1;
        gc.gridy = 5;

        p.add(maxYVal, gc);

        title = BorderFactory.createTitledBorder("Y Fit Data");
        p.setBorder(title);

        panel.add(p);
        panel.setFocusable(false);
        return panel;

    }

    public Component createToolsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        // panel.setPreferredSize(new Dimension(200 , 30) );
        GridBagConstraints gc = new GridBagConstraints();
        //gc.weightx = gc.weighty = 1.0;
        //gc.fill = GridBagConstraints.BOTH;

        gc.gridx = 0;
        gc.gridy = 0;
        //gc.anchor = GridBagConstraints.WEST;

        panel.add(createShapeTools(), gc);

        gc.gridx = 0;
        gc.gridy = 1;
        //gc.anchor = GridBagConstraints.WEST;

        panel.add(createZoomTools(), gc);

        //gc.gridx = 0;
        //gc.gridy = 2;
        //gc.anchor = GridBagConstraints.WEST;

        //p.add(createTestButton(), gc);
//
//        gc.gridx = 0;
//        gc.gridy = 3;
//        gc.anchor = GridBagConstraints.WEST;
        gc.gridx = 0;
        gc.gridy = 0;
        //panel.add(p, gc);
        return panel;
    }

    public Component createSavePanel() {
        Panel panel = new Panel(new GridBagLayout());
        // panel.setPreferredSize(new Dimension(200 , 30) );
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;

        JPanel p = new JPanel();
        p.setLayout(layout);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;

        p.add(createSetSavePathPanel(), gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.anchor = GridBagConstraints.WEST;

        p.add(createSaveAsPanel(), gc);
        gc.gridx = 0;
        gc.gridy = 2;
        gc.anchor = GridBagConstraints.WEST;

        p.add(createSaveRoiAsPanel(), gc);

        gc.gridx = 0;
        gc.gridy = 0;

        panel.add(p, gc);
        return panel;
    }

    public Component createShapeTools() {

        Panel panel = new Panel();

        JPanel p = new JPanel();

        p.add(createToolButton(pathToIcons + "black_rectangle_small.png", Toolbar.RECTANGLE, "Rectangle Tool"));
        p.add(createToolButton(pathToIcons + "oval_black.png", "elliptical", "Ellpise"));
        p.add(createToolButton(pathToIcons + "polygon_small_2.png", Toolbar.FREEROI, "Free hand"));

        Log.log(pathToIcons, true);

        title = BorderFactory.createTitledBorder("Shapes");
        p.setBorder(title);
        panel.add(p);
        panel.add(createPointTools());
        panel.setFocusable(false);
        return panel;

    }

    public Component createZoomTools() {

        Panel panel = new Panel();

        JPanel p = new JPanel();

        p.add(createToolButton(pathToIcons + "hand_small.png", Toolbar.HAND, "Hand tool"));
        p.add(createToolButton(pathToIcons + "mag_small.png", Toolbar.MAGNIFIER, "<html>Left Mouse (in image) = ZoomIn,"
                + " <br>Middle Mouse Click(in image) = ZoomOut</html>"));

        title = BorderFactory.createTitledBorder("Magnify");
        p.setBorder(title);
        panel.add(createLineTools());
        panel.add(p);
        panel.setFocusable(false);
        return panel;

    }

    public Component createLineTools() {

        Panel panel = new Panel();

        JPanel p = new JPanel();

        p.add(createToolButton(pathToIcons + "line_small.png", Toolbar.LINE, "Line tool"));
        p.add(createToolButton(pathToIcons + "multiLine_small.png", Toolbar.POLYLINE, "Poly Line Tool"));
        p.add(createToolButton(pathToIcons + "angle_small.png", Toolbar.ANGLE, "Angle Tool"));
        p.add(createToolButton(pathToIcons + "polygon_small.png", Toolbar.FREELINE, "Free hand Line"));

        title = BorderFactory.createTitledBorder("Line");
        p.setBorder(title);
        panel.add(p);
        panel.setFocusable(false);
        return panel;

    }

    public Component createPointTools() {

        Panel panel = new Panel();

        JPanel p = new JPanel();

        p.add(createToolButton(pathToIcons + "point_small.png", Toolbar.POINT, "Point tool"));
        p.add(createToolButton(pathToIcons + "crosshair_small.png", Toolbar.CROSSHAIR, "Cross Hair Tool"));

        title = BorderFactory.createTitledBorder("Point");
        p.setBorder(title);
        panel.add(p);
        panel.setFocusable(false);
        return panel;

    }
    public ArrayList<JButton> toolButtons = new ArrayList<JButton>();

    public JButton createToolButton(String iconPath, final int toolId, String toolTip) {

        final JButton b = new JButton();
        b.setName(toolId+"");
        b.setIcon(createImageIcon(iconPath, "tool = " + toolId));
        b.setPreferredSize(new Dimension(25, 25));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                IJ.setTool(toolId);
                b.setBackground(Color.GREEN);
                for (JButton but : toolButtons) {
                    if (but.equals(b)) {
                        continue;
                    } else {
                        but.setBackground(null);
                    }
                }
                
                //sync the context menu
                if(impp!=null){
                    if(impp.mainmenu.cmenuToolItems!=null){
                        for(CheckboxMenuItem cmi : impp.mainmenu.cmenuToolItems){
                            
                            if(cmi.getName().equals(b.getName())){
                                cmi.setState(true);
                      
                            }
                            else{
                                cmi.setState(false);
                            }
                            
                        }
                    }
                }
            }
        });

        if (toolId == Toolbar.getToolId()) { //if the tool is equal to the default then set it to grren
            b.setBackground(Color.GREEN);
        }

        if (toolTip != null) {
            b.setToolTipText(toolTip);
        }

        //add to list of tools
        toolButtons.add(b);
        return b;

    }

    public JButton createToolButton(String iconPath, final String toolId, String toolTip) {

        final JButton b = new JButton();
        b.setName(toolId);
        b.setIcon(createImageIcon(iconPath, toolId));
        b.setPreferredSize(new Dimension(25, 25));
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                IJ.setTool(toolId);
                b.setBackground(Color.GREEN);
                for (JButton but : toolButtons) {
                    if (but.equals(b)) {
                        continue;
                    } else {
                        but.setBackground(null);
                    }
                }
                
                //sync the context menu
                if(impp!=null){
                    if(impp.mainmenu.cmenuToolItems!=null){
                        for(CheckboxMenuItem cmi : impp.mainmenu.cmenuToolItems){
                            
                            if(cmi.getName().equals(b.getName())){
                                cmi.setState(true);
                               
                            }
                            else{
                                cmi.setState(false);
                            }
                            
                        }
                    }
                }
            }
        });
        if (toolId.equals(IJ.getToolName())) {
            b.setBackground(Color.GREEN);
        }
        if (toolTip != null) {
            b.setToolTipText(toolTip);
        }
        //add to list of tools
        toolButtons.add(b);
        return b;

    }

    public void showLegend() {
        Log.log("show legend called ", win.debug);
        legend = new Legend(this.impp, win.debug);

    }

    public void toggleEastPanel() {
        if (eastPanel != null && isEastShowing == true) {
            this.remove(eastPanel);
            isEastShowing = false;
        } else if (eastPanel == null) {
            eastPanel = (Panel) createEast();
            this.add(eastPanel, BorderLayout.EAST);
            isEastShowing = true;
        } else {
            this.add(eastPanel, BorderLayout.EAST);
            isEastShowing = true;
        }
        this.repaint();
        win.pack();
    }

    public void hideSouth() {
        if (southPanel == null) {
            Log.log("SouthPanel was null when hide was called ", win.debug);
            isSouthShowing = false;
            return;
        }
        isSouthShowing = false;
        this.remove(southPanel);
        this.win.pack();

    }

    public void showSouth() {
        if (southPanel == null) {
            Log.log("SouthPanel was null when show was called ", win.debug);
            return;
        }

        this.add(southPanel, BorderLayout.SOUTH);
        isSouthShowing = true;
        //this.win.pack();
    }

    public void toggleSouthPanel() {
        if (southPanel != null && isEastShowing == true) {
            this.remove(eastPanel);
            isSouthShowing = false;
        } else if (southPanel == null) {
            southPanel = (Panel) createEast();
            this.add(southPanel, BorderLayout.SOUTH);
            isSouthShowing = true;
        } else {
            this.add(southPanel, BorderLayout.SOUTH);
            isSouthShowing = true;
        }
        this.repaint();
        win.pack();
    }

    public void toggleXPlotPanel() {

        gc.gridx = 1;
        gc.gridy = 1;

        if (xPlotPanel != null && isXPlotShowing == true) {
            imagePanelContainer.remove(xPlotPanel);
            isXPlotShowing = false;
            if (xpp != null && xpp.bgThread != null) {
                Log.log("thread stopped", win.debug);
                if (xpp.bgThread.isAlive()) {
                    xpp.stop();
                }
            }
        } else if (xPlotPanel == null) {
            xPlotPanel = (Panel) createXPlotPanel();
            imagePanelContainer.add(xPlotPanel, gc);
            isXPlotShowing = true;
            Log.log("plot is null .... ", win.debug);

        } else {
            imagePanelContainer.add(xPlotPanel, gc);
            isXPlotShowing = true;
            Log.log("liveMode = " + xpp.liveMode, win.debug);
            if (xpp.liveMode == false) {
                xpp.restartLive();
                Log.log("restarting", win.debug);
            }
        }

        imagePanelContainer.repaint();
        this.repaint();
        win.pack();
    }

    public void toggleYPlotPanel() {

        gc.gridx = 0;
        gc.gridy = 0;

        if (yPlotPanel != null && isYPlotShowing == true) {
            imagePanelContainer.remove(yPlotPanel);
            isYPlotShowing = false;
            if (ypp != null && ypp.bgThread != null) {
                Log.log("thread stopped", win.debug);
                if (ypp.bgThread.isAlive()) {
                    ypp.stop();
                }
            }
        } else if (yPlotPanel == null) {
            yPlotPanel = (Panel) createYPlotPanel();
            imagePanelContainer.add(yPlotPanel, gc);
            isYPlotShowing = true;
            Log.log("plot is null .... ", win.debug);

        } else {
            imagePanelContainer.add(yPlotPanel, gc);
            isYPlotShowing = true;
            Log.log("liveMode = " + ypp.liveMode, win.debug);
            if (ypp.liveMode == false) {
                ypp.restartLive();
                Log.log("restarting", win.debug);
            }
        }

        imagePanelContainer.repaint();
        this.repaint();
        win.pack();
    }

    public Component createPlayStopPanel() {
        Panel panel = new Panel();
        JPanel p = new JPanel(new FlowLayout(1));
        final Dimension d = new Dimension(28, 25);

        playButton = new JButton();
        playButton.setIcon(createImageIcon(pathToIcons + "play_blue_small.png", "play"));
        if (imagePanel != null) {
            if (imagePanel.timer != null && imagePanel.timer.isRunning()) {
                playButton.setBackground(Color.GREEN);
            }
        }
        playButton.setToolTipText("Play Live");
        
        pauseButton = new JButton();
        pauseButton.setIcon(createImageIcon(pathToIcons + "pause_blue_small.png", "pause"));
        pauseButton.setToolTipText("Pause Live");

        final JButton b3 = new JButton();
        b3.setIcon(createImageIcon(pathToIcons + "black_cam_small.png", "snap"));
        b3.setToolTipText("<html>Snap Shot<br>Quick Keys = ctrl+g</html>");

        //JButton b3 = new JButton();
        //b3.setIcon( createImageIcon(pathToIcons + "play_blue_small.png", "play") );
        playButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (imagePanel != null) {
                    if (!imagePanel.timer.isRunning()) {
                        imagePanel.timer.start();
                    }
                    playButton.setBackground(Color.GREEN);
                    pauseButton.setBackground(null);
                    
                    if(impp.mainmenu!=null){
                        impp.mainmenu.start.setEnabled(false);
                        impp.mainmenu.stop.setEnabled(true);
                    }

                }
            }
        });

        pauseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (imagePanel != null) {
                    if (imagePanel.timer.isRunning()) {
                        imagePanel.timer.stop();
                    }
                    playButton.setBackground(null);
                    pauseButton.setBackground(Color.GREEN);
                    
                    if(impp.mainmenu!=null){
                        impp.mainmenu.start.setEnabled(true);
                        impp.mainmenu.stop.setEnabled(false);
                    }
                }
            }
        });

        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (impp.imageUpdater != null) {

                    impp.imageUpdater.makeImageCopy(impp.getTitle());

                }
            }
        });

        playButton.setPreferredSize(d);
        pauseButton.setPreferredSize(d);
        b3.setPreferredSize(d);

        p.add(playButton);
        p.add(pauseButton);
        p.add(b3);
        // p.add(b3);
        title = BorderFactory.createTitledBorder("Controls");
        p.setBorder(title);
        panel.add(p);
        panel.setFocusable(false);
        return panel;
    }

    public Component createBackGroundSubtractPanel() {
        Panel panel = new Panel();
        // panel.setPreferredSize(new Dimension(200 , 30) );
        JPanel p = new JPanel(new FlowLayout(2));

        saveBackGroundButton = new JButton("Save");

        backGroundSubOnButton = new JButton("On");

        final JButton b3 = new JButton("Reset");

        saveBackGroundButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (impp.imageUpdater != null) {
                    impp.imageUpdater.saveBackground();
                    saveBackGroundButton.setBackground(Color.yellow);
                    saveBackGroundButton.setText("Saved");
                }
            }
        });

        backGroundSubOnButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (impp.imageUpdater != null) {
                    if (impp.imageUpdater.isSubtraction) {
                        impp.imageUpdater.isSubtraction = false;
                        backGroundSubOnButton.setBackground(null);
                        if(impp.mainmenu!=null ){
                            if(impp.mainmenu.subtractionOn!=null){
                                impp.mainmenu.subtractionOn.setState(false);
                            }
                        }
                        
                        //b2.setText("Off");
                    } else {
                        impp.imageUpdater.isSubtraction = true;
                        backGroundSubOnButton.setBackground(Color.GREEN);
                        //b2.setText("On");
                        if(impp.mainmenu!=null ){
                            if(impp.mainmenu.subtractionOn!=null){
                                impp.mainmenu.subtractionOn.setState(true);
                            }
                        }
                    }

                }

            }
        });

        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (impp.imageUpdater != null) {

                    impp.imageUpdater.pixelBytes = null;
                    saveBackGroundButton.setBackground(null);
                    backGroundSubOnButton.setSelected(false);
                    impp.imageUpdater.isSubtraction = false;
                    backGroundSubOnButton.setBackground(null);
                    saveBackGroundButton.setText("Save");
                    // b2.setText("Off");
                }
            }
        });

        p.add(saveBackGroundButton);
        p.add(backGroundSubOnButton);
        p.add(b3);

        title = BorderFactory.createTitledBorder("Background Subtraction");
        p.setBorder(title);
        p.setFocusable(false);
        panel.add(p);
        panel.add(createFindEdgesPanel());
        panel.setFocusable(false);

        return panel;
    }

    public Component createFindEdgesPanel() {
        Panel panel = new Panel(new BorderLayout(2, 2));
        // panel.setPreferredSize(new Dimension(200 , 30) );
        JPanel p = new JPanel(new FlowLayout(2));

        edgesButton = new JButton("Off");

        edgesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (impp != null) {
                    if (impp.findedges) {
                        impp.findedges = false;
                        edgesButton.setBackground(null);
                        edgesButton.setText("Off");
                        if(impp.mainmenu!=null){
                            impp.mainmenu.findEdges.setState(false);
                        }
                        if (impp.imageUpdater == null) {
                            //impp.resetPixels();
                            Log.log("reset ", win.debug);
                        }
                    } else {
                        impp.findedges = true;
                        edgesButton.setBackground(Color.GREEN);
                        edgesButton.setText("On");
                        if (impp.imageUpdater == null) {
                            IJ.runPlugIn(impp, "ij.plugin.filter.Filters", "edge");
                        }
                        if(impp.mainmenu!=null){
                            impp.mainmenu.findEdges.setState(true);
                        }
                    }

                }

            }
        });

        p.add(edgesButton);

        title = BorderFactory.createTitledBorder("Edges...");
        p.setBorder(title);
        p.setFocusable(false);
        panel.add(p, BorderLayout.NORTH);
        panel.setFocusable(false);
        return panel;
    }

    public String getFullSavePath() {
        String s = "";
        if (savePath == null && savePath.equals("")) {
            Log.log(s, win.debug);
            return s;
        }

        if (fileName == null || fileName.equals("")) {
            if (cam != null) {
                fileName = cam.getId();
            } else {
                fileName = "defaultName";
            }

        }

        s = savePath + "/" + fileName;

        if (saveAsSeq) {
            s += "_" + seqCount;
            seqCount++;
        } else {
            seqCount = 1;
        }

        Log.log(s, win.debug);
        return s;

    }

    public Component createSetSavePathPanel() {
        Panel panel = new Panel();
        JPanel p = new JPanel(new GridLayout(2, 1));

        JPanel p1 = new JPanel(new FlowLayout(2));
        JPanel p2 = new JPanel(new FlowLayout(2));

        savePath = "";
        fileName = "";

        final JTextField text = makeSetpoint(15);
        //text.setText(savePath);
        JLabel label = new JLabel("Save Path: ");
        final JTextField text2 = makeSetpoint(15);
        JLabel label2 = new JLabel("File Name: ");
        JButton b1 = new JButton("Browse");
        JLabel label3 = new JLabel("Is sequence : ");
        final JCheckBox cb = new JCheckBox();

        //add listner to save path text field, that will update the global save path variable
        text.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {

                savePath = text.getText();
                Log.log("Save Path changed to : " + savePath, win.debug);
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                savePath = text.getText();
                Log.log("Save Path changed to : " + savePath, win.debug);

            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                savePath = text.getText();
                Log.log("Save Path changed to : " + savePath, win.debug);
            }
        });

        //add listner to filename text field, that will update the global fileName variable
        text2.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent de) {

                fileName = text2.getText();
                Log.log("File Name changed to : " + fileName, win.debug);
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                fileName = text2.getText();
                Log.log("File Name changed to : " + fileName, win.debug);
            }

            @Override
            public void changedUpdate(DocumentEvent de) {
                fileName = text2.getText();
                Log.log("File Name changed to : " + fileName, win.debug);
            }
        });

        //check box will set a boolena that will append a counter to the filename
        cb.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                if (cb.isSelected()) {
                    saveAsSeq = true;

                } else {
                    saveAsSeq = false;
                    seqCount = 1;
                }
            }
        });

        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                JFileChooser fc = new JFileChooser();
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = fc.showOpenDialog(eastPanel);

                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    text.setText(file.getAbsolutePath());

                    Log.log(returnVal + " : " + file.getAbsolutePath(), win.debug);
                }

            }
        });

        p1.add(label);
        p1.add(text);
        p1.add(b1);

        p2.add(label2);
        p2.add(text2);
        p2.add(label3);
        p2.add(cb);

        p.add(p1);
        p.add(p2);

        title = BorderFactory.createTitledBorder("Set File Save Path (Not required)");
        p.setBorder(title);
        panel.setFocusable(false);
        panel.add(p);
        return panel;
    }

    public Component createSaveAsPanel() {
        saver = new FileSaver(this.impp);

        Panel panel = new Panel();
        // panel.setPreferredSize(new Dimension(200 , 30) );
        JPanel p = new JPanel(new GridLayout(2, 1));
        JPanel p1 = new JPanel(new FlowLayout(2));
        JPanel p2 = new JPanel(new FlowLayout(2));

        JButton b1 = new JButton("JPEG");
        JButton b2 = new JButton("PNG");
        JButton b3 = new JButton("TIFF");
        JButton b4 = new JButton("PGM");
        JButton b5 = new JButton("RAW");
        JButton b6 = new JButton("TEXT");
        JButton b7 = new JButton("XDAT");
        JButton b8 = new JButton("YDAT");

        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // saver.saveAsJpeg();
                saver = new FileSaver(impp);
                if (savePath == null || savePath.equals("")) {
                    saver.saveAsJpeg();
                } else {
                    saver.saveAsJpeg(getFullSavePath() + ".jpg");
                }
                //Log.log(getFullSavePath()+".jpg", win.debug);

            }
        });
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                saver = new FileSaver(impp);
                if (savePath == null || savePath.equals("")) {
                    saver.saveAsPng();
                } else {
                    saver.saveAsPng(getFullSavePath() + ".png");
                }
            }
        });
        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                saver = new FileSaver(impp);
                if (savePath == null || savePath.equals("")) {
                    saver.saveAsTiff();
                } else {
                    saver.saveAsTiff(getFullSavePath() + ".tif");
                }
            }
        });
        b4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (savePath == null || savePath.equals("")) {
                    Helpers.saveAsPgm(impp, null, win.debug);
                } else {
                    Helpers.saveAsPgm(impp, getFullSavePath() + ".pgm", win.debug);
                }
            }
        });
        b5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                saver = new FileSaver(impp);
                if (savePath == null || savePath.equals("")) {
                    saver.saveAsRaw();
                } else {
                    saver.saveAsRaw(getFullSavePath() + ".raw");
                }
            }
        });
        b6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                saver = new FileSaver(impp);
                if (savePath == null || savePath.equals("")) {
                    saver.saveAsText();
                } else {
                    saver.saveAsText(getFullSavePath() + ".txt");
                }
            }
        });
        
        b7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //impp.copy();
                //saver = new FileSaver( ImagePlus.getClipboard() );
                if (savePath == null || savePath.equals("")) {
                    Helpers.saveAsDat(impp, null, true, win.debug);
                } else {
                    Helpers.saveAsDat(impp, getFullSavePath() + ".dat", true, win.debug);
                }
            }
        });
        b8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //impp.copy();
                //saver = new FileSaver( ImagePlus.getClipboard() );
                if (savePath == null || savePath.equals("")) {
                    Helpers.saveAsDat(impp, null, false, win.debug);
                } else {
                    Helpers.saveAsDat(impp, getFullSavePath() + ".dat", false, win.debug);
                }
            }
        });

        p1.add(b1);
        p1.add(b2);
        p1.add(b3);
        p1.add(b7);
        p2.add(b4);
        p2.add(b5);
        p2.add(b6);
        p2.add(b8);
        
        p.add(p1);
        p.add(p2);
        title = BorderFactory.createTitledBorder("Save Image As");
        p.setBorder(title);
        panel.setFocusable(false);
        panel.add(p);
        return panel;
    }

    public Component createSaveRoiAsPanel() {
        saver = new FileSaver(this.impp);

        Panel panel = new Panel();
        // panel.setPreferredSize(new Dimension(200 , 30) );
        JPanel p = new JPanel(new GridLayout(2, 1));
        JPanel p1 = new JPanel(new FlowLayout(2));
        JPanel p2 = new JPanel(new FlowLayout(2));

        JButton b1 = new JButton("JPEG");
        JButton b2 = new JButton("PNG");
        JButton b3 = new JButton("TIFF");
        JButton b4 = new JButton("PGM");
        JButton b5 = new JButton("RAW");
        JButton b6 = new JButton("TEXT");
        JButton b7 = new JButton("XDAT");
        JButton b8 = new JButton("YDAT");

        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                // saver.saveAsJpeg();
                impp.copy();
                saver = new FileSaver(ImagePlus.getClipboard());

                if (savePath == null || savePath.equals("")) {
                    saver.saveAsJpeg();
                } else {
                    saver.saveAsJpeg(getFullSavePath() + ".jpg");
                }
                //Log.log(getFullSavePath()+".jpg", win.debug);

            }
        });
        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                impp.copy();
                saver = new FileSaver(ImagePlus.getClipboard());
                if (savePath == null || savePath.equals("")) {
                    saver.saveAsPng();
                } else {
                    saver.saveAsPng(getFullSavePath() + ".png");
                }
            }
        });
        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                impp.copy();
                saver = new FileSaver(ImagePlus.getClipboard());
                if (savePath == null || savePath.equals("")) {
                    saver.saveAsTiff();
                } else {
                    saver.saveAsTiff(getFullSavePath() + ".tif");
                }
            }
        });
        b4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //impp.copy();
                //saver = new FileSaver( ImagePlus.getClipboard() );
                if (savePath == null || savePath.equals("")) {
                    Helpers.saveRoiAsPgm(impp, null, win.debug);
                } else {
                    Helpers.saveRoiAsPgm(impp, getFullSavePath() + ".pgm", win.debug);
                }
            }
        });
        b5.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                impp.copy();
                saver = new FileSaver(ImagePlus.getClipboard());
                if (savePath == null || savePath.equals("")) {
                    saver.saveAsRaw();
                } else {
                    saver.saveAsRaw(getFullSavePath() + ".raw");
                }
            }
        });
        b6.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                impp.copy();
                saver = new FileSaver(ImagePlus.getClipboard());
                if (savePath == null || savePath.equals("")) {
                    saver.saveAsText();
                } else {
                    saver.saveAsText(getFullSavePath() + ".txt");
                }
            }
        });
        b7.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //impp.copy();
                //saver = new FileSaver( ImagePlus.getClipboard() );
                if (savePath == null || savePath.equals("")) {
                    Helpers.saveRoiAsDat(impp, null, true, win.debug);
                } else {
                    Helpers.saveRoiAsDat(impp, getFullSavePath() + ".dat", true, win.debug);
                }
            }
        });
        b8.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                //impp.copy();
                //saver = new FileSaver( ImagePlus.getClipboard() );
                if (savePath == null || savePath.equals("")) {
                    Helpers.saveRoiAsDat(impp, null, false, win.debug);
                } else {
                    Helpers.saveRoiAsDat(impp, getFullSavePath() + ".dat", false, win.debug);
                }
            }
        });

        p1.add(b1);
        p1.add(b2);
        p1.add(b3);
        p1.add(b7);
        p2.add(b4);
        p2.add(b5);
        p2.add(b6);
        p2.add(b8);
        p.add(p1);
        p.add(p2);
        title = BorderFactory.createTitledBorder("Save ROI As");
        p.setBorder(title);
        panel.setFocusable(false);
        panel.add(p);
        return panel;
    }

    
     public Component createTurnOffAverage(){
        JPanel p = new JPanel(new FlowLayout(2));
        
        final JCheckBox box = new JCheckBox();
        
        JLabel label = new JLabel("Averaging Off :");
        
        box.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
           
                if(box.isSelected()) {
                    
                    if(xpp!=null) xpp.isAveraged = false;
                    if(ypp!=null) ypp.isAveraged = false;
                    
                } 
                else{
                    if(xpp!=null) xpp.isAveraged = true;
                    if(ypp!=null) ypp.isAveraged = true;
                }
                
               if(xpp!=null) { xpp.imageUpdated(impp);}
               if(ypp!=null) { ypp.imageUpdated(impp);}
            
            }
        });
        
        title = BorderFactory.createTitledBorder("AverageOff");
        p.setBorder(title);
        
        p.add(label);
        p.add(box);
        
        return p;
        
    }
    
    public Component createLutDropDownPanel() {

        JPanel p = new JPanel(new FlowLayout(2));
        if (impp != null) {
            originalLut = impp.getProcessor().getLut();
        }

        String[] luts = new String[win.luts.map.size() + 1];
        luts[0] = "None";
        int i = 1;
        for (String s : win.luts.getSortedKeys()) {
            luts[i] = s;
            i++;
        }

        lutChoiceBox = new JComboBox<String>(luts);

        lutChoiceBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {

                if (lutChoiceBox.getSelectedItem().equals("None")) {
                    impp.getProcessor().setLut(originalLut);
                    //sync popup menu
                    for(CheckboxMenuItem cmi : impp.mainmenu.cmenuLutItems){
                           if(cmi.getLabel().equals("Reset LUT")){
                               cmi.setState(true);
                           }
                           else{
                               cmi.setState(false);
                           }
                     }
                          
                } else {
                    changeLUT(win.luts.map.get(lutChoiceBox.getSelectedItem()));
                    
                    //for syncing popmenu
                     for(CheckboxMenuItem cmi : impp.mainmenu.cmenuLutItems){
                        if(cmi.getLabel().equals(lutChoiceBox.getSelectedItem())){
                            cmi.setState(true);
                        }
                        else{
                            cmi.setState(false);
                        }
                    }
                }
            }
        });

        JButton resetLut = new JButton("Reset");
        resetLut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                impp.getProcessor().setLut(originalLut);

                lutChoiceBox.setSelectedItem("None");
                
                //sync popup menu
                for(CheckboxMenuItem cmi : impp.mainmenu.cmenuLutItems){
                    if(cmi.getLabel().equals("Reset LUT")){
                        cmi.setState(true);
                    }
                    else{
                        cmi.setState(false);
                    }
                }

            }
        });

        title = BorderFactory.createTitledBorder("Look Up Tables (LUT)");
        p.setBorder(title);

        p.add(lutChoiceBox);
        p.add(resetLut);
        p.setFocusable(false);  
        return p;

    }

    
    
    public Component createCamDropDown() {
        dropPanel = new Panel();

        JPanel p = new JPanel(new FlowLayout());

        if (cams == null) {
            return dropPanel;
        }

        if (cam == null) {
            cam = new CameraConfig("NONE", this.impp.getTitle());
            cams.cameras.add(cam);
            cams.map.put(cam.getId(), cam);
        }

        camChooserLabel = makeReadback(cam.getName(), new Dimension(140, 20));
        p.add(camChooserLabel);

       // final Choice chooser = new Choice();
        String[] ids = new String[cams.map.size()];
        int i = 0;

        //loop through cameras
        for (CameraConfig c : cams.cameras) {
            ids[i] = c.getId();
            i++;
            // Log.log( c.getId() , win.debug);
        }

        //JComboBox of camera ids. This is obtained from the Config Object Class handed down from ADWindow
        camChooser = new JComboBox<String>(ids);
        camChooser.setSelectedItem(this.cam.getId());
        camChooser.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent ie) {
                Log.log(cams.map.get(camChooser.getSelectedItem()).getId() + " was selected", win.debug);

                cam = cams.map.get(cams.map.get(camChooser.getSelectedItem()).getId()); //main panel cam
                win.cam = cam; //set ADWindow cam
                imagePanel.cam = cam; //update imagepanel.cam

                imagePanel.streamChanged = true; //change this boolean in imagePanel so it triggers a camera change 

                camChooserLabel.setText(cam.getName()); //set the text of the label
                if (impp.imageUpdater != null) {
                    
                    impp.imageUpdater.cam = cam;    //must change the cam object in ImagePlusPLus
                    Log.log(impp.imageUpdater.cam.getName() + "=====" + imagePanel.streamer.cam.getName() + "============", win.debug); // make sure that these match
                
                } else {    //this was a STATIC image
                
                    savedImpp = impp;
                    imagePanel.startStream(cam.getConnectionType());
                    imagePanel.streamChanged = true;
                    //imagePanel.changeStream();

                    //impp.imageUpdater = imagePanel.streamer;
                    imagePanel.timer.start();
                }

                //if not BC1, BC2, BC3, BC4, then removeSouthPanel, else Draw it.
                //site specific panel
                if (isCEBAFBCandMakeMonitor(cam)) {
                    showSouth();
                } else {
                    hideSouth();
                }

            }
        });
        p.add(camChooser);

        Log.log(" Cam Drop Down Done....  ", win.debug);

        dropPanel.add(p);
        return dropPanel;
    }

    public Component createGammaAdjust() {
        Panel panel = new Panel();
        panel.setFocusable(false);
        JPanel p = new JPanel();

        title = BorderFactory.createTitledBorder("Gamma Adjust");
        p.setBorder(title);

        final JSlider slider = new JSlider(5, 500, 100);
        final JLabel label = new JLabel();
        final JButton b1 = new JButton("Reset");

        //label.setForeground(Color.WHITE);
        //label.setBackground(Color.BLUE);
        label.setText(impp.gamma + "");
        label.setPreferredSize(new Dimension(30, 25));

        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent ce) {
                double gamma = ((double) slider.getValue() / (double) 100.00);

                label.setText(gamma + "");
                if (impp.imageUpdater == null) {

                    impp.getProcessor().gamma(gamma);
                    impp.updateAndDraw();
                } //if this is not a live stream then process the gamma filter
                else {
                    impp.gamma = gamma;
                }
            }
        });

        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                slider.setValue(100);

                double gamma = ((double) slider.getValue() / (double) 100.00);

                label.setText(gamma + "");
                if (impp.imageUpdater == null) {
                    //impp.getProcessor().gamma(gamma);
                    //impp.updateAndDraw();
                } //if this is not a live stream then process the gamma filter
                else {
                    impp.gamma = gamma;
                }

            }
        });
        p.add(label);
        p.add(slider);
        p.add(b1);

        panel.add(p);
        return panel;
    }

    public Component createBrightnessAdjust() {
        Panel panel = new Panel();
        JPanel p = new JPanel();

        title = BorderFactory.createTitledBorder("Brightness/ Contrast Adjust");
        p.setBorder(title);

        JButton button = new JButton("Adjust");
        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                IJ.runPlugIn("ij.plugin.frame.ContrastAdjuster", "");
            }
        });

        JButton button1 = new JButton("WL");
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                IJ.runPlugIn("ij.plugin.frame.ContrastAdjuster", "wl");
            }
        });

        /**
         * JButton button2 = new JButton("Threshold");
         * button2.addActionListener(new ActionListener() {
         *
         * @Override public void actionPerformed(ActionEvent ae) {
         * IJ.runPlugIn("ij.plugin.frame.ThresholdAdjuster",""); } });*
         */
        p.add(button);
        p.add(button1);
        //p.add(button2);
        panel.add(createPlayStopPanel());
        panel.add(p);

        return panel;
    }

    public Component createMessageLabel() {

        final JPanel panel = new JPanel(new BorderLayout(2,2)) ;      
        final JPanel p1 = new JPanel() ;      
        
        
        Dimension d2 = new Dimension(150, 22);
        //Dimension d2 = null;

        final String defaultString = "Check to get Inserted Viewer";
        messageLabel = makeReadback(defaultString,null);
     

        final JCheckBox getViewerSigmas = new JCheckBox();

        statusLight = new JLabel();
        statusLight.setOpaque(true);
        statusLight.setPreferredSize(new Dimension(20,20));
        statusLight.setBackground(Color.GREEN);
        Border border= BorderFactory.createLineBorder(Color.BLACK);
        statusLight.setBorder(border);
        statusLight.setToolTipText("Yellow if ViewerList is updating (more than 24 hrs old), green otherwise");
        
        final JPanel labels = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        final JLabel label1 = new JLabel("Viewer:");
        final JLabel label2 = new JLabel("SigmaX:");        
        final JLabel label3 = new JLabel("SigmaY:");
        final JLabel label4 = new JLabel("Aspect Ratio:");
        
        final JLabel label5 =  makeReadback("ITVXXXX" , d2);
        final JLabel label6 =  makeReadback("1.023",d2);
        final JLabel label7 =  makeReadback("2.345",d2);
        final JLabel label8 =  makeReadback("1:1.2",d2);

        
        c.gridx = 0;
        c.gridy = 0;
        c.insets = new Insets(2,2,2,2);
        labels.add(label1, c );
        
        c.gridx = 1;
        c.gridy = 0;
        labels.add(label5, c);
        
        c.gridx = 0;
        c.gridy = 1;
        labels.add(label2,c);
        
        c.gridx = 1;
        c.gridy = 1;
        labels.add(label6,c);
        
        c.gridx = 0;
        c.gridy = 2;
        labels.add(label3,c);
        
        c.gridx = 1;
        c.gridy = 2;
        labels.add(label7,c);
        
        c.gridx = 0;
        c.gridy = 3;
        labels.add(label4,c);
        
        c.gridx = 1;
        c.gridy = 3;
        labels.add(label8,c);
        
        title = BorderFactory.createTitledBorder("Viewer Info:");
        labels.setBorder(title);
        //labels.setPreferredSize(d);

        //run to get get viewer names
        final RunSystemCommand run = new RunSystemCommand(win, win.debug, 50);
        run.setLabels(labels, label5, label6, label7, label8);
        run.messageLabel = messageLabel;
        run.statusLight = statusLight;
        
        getViewerSigmas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                if (getViewerSigmas.isSelected()) {
                    run.run = true;
                    new Thread(run).start(); //start the thread
                    panel.add(labels, BorderLayout.EAST);
                    messageLabel.setText("Building viewer list.... takes ~1 min ");
                    statusLight.setBackground(Color.yellow);

                } else {
                    run.run = false;
                    messageLabel.setText( defaultString );
                    panel.remove(labels);
                    statusLight.setBackground(Color.GREEN);
                }
               
                panel.repaint();
                win.pack();

            }
        });

        //panel.setBackground(Color.BLUE);
        
        
        
        p1.add(getViewerSigmas);
        p1.add(messageLabel);
        p1.add(statusLight);
       
        
        panel.add(p1, BorderLayout.WEST);
        
        return panel;
    }

    public Component createSouth() {
        southPanel = new Panel(new BorderLayout(2, 2));

        //south.setBackground(Color.BLUE);
        southPanel.add(createMessageLabel(), BorderLayout.SOUTH);

        return southPanel;
    }

    public Component createInfoPanel(){
    
        JPanel panel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gc = new GridBagConstraints();
       
        gc.gridx = 0;
       gc.gridy = 0;
       gc.fill = GridBagConstraints.HORIZONTAL;
       gc.gridwidth = 2;
       gc.insets = new Insets(2,2,2,2);
        
       panel.add(createDescriptionPanel(), gc);
        
        gc.gridx = 0;
       gc.gridy = 1;
       gc.gridwidth = 1;
       
        panel.add(createHelpLink() , gc);
        
        gc.gridx = 1;
       gc.gridy = 1;
        panel.add(createUsefulLinks(), gc);
        
       
        return panel;
    }
    
     public Component createHelpLink(){
    
       
        JPanel p = new JPanel();
        String link = "< Click here for Help > ";
        SwingLink help = new SwingLink(link , "https://github.com/bfreeman23810/adviewer/wiki/");
        
        title = BorderFactory.createTitledBorder("ClickForHelp");
        p.setBorder(title);
        p.add(help);
       
        return p;
    }
     
    public Component createUsefulLinks(){
    
       int NUMLINKS = 4;
        JPanel p = new JPanel(new GridLayout(NUMLINKS,0));
        
       
        SwingLink imagej = new SwingLink("ImageJ               " , "http://rsb.info.nih.gov/ij/");
        SwingLink ad = new SwingLink("Area Detector" , "https://github.com/areaDetector");
        SwingLink camboloza = new SwingLink("Camboloza" , "http://www.charliemouse.com/code/cambozola/");
        
        p.add(imagej);
        p.add(ad);
        p.add(camboloza);
       
        title = BorderFactory.createTitledBorder("Links");
        p.setBorder(title);
       
        return p;
    } 
    
    public Component createDescriptionPanel(){
    
        JPanel p = new JPanel();
        
        JLabel text = new JLabel();
        
        String s = "<html><b>AppName</b>:   ADViewer v" + win.version;
        s += "<br>";
        s += "<b>Author:</b>   Brian Freeman (bfreeman@jlab.org)";
        s += "<br><br>";
        s += "<b>Purpose:</b> Application used for viewing live  <br>";
        s += "EPICS streams, and live MJPEG streams. Also utilizes <br>";
        s += "an ImageJ back end to incooperate some image <br>";
        s += "processing capablities. See Help below for more <br>";
        s += "information.<br>";
        s += "";
        s += "";
        s += "</html>";
        
        text.setText(s);
        p.add(text);
        
        title = BorderFactory.createTitledBorder("Description");
        p.setBorder(title);
        return p;
        
    }
    
    public void changeLUT(String path) {
        //IJ.run("Rainbow_RGB");
        Log.log(path, win.debug);
        try {
            cm = LutLoader.open(path);
            lut = new LUT(cm, impp.getWidth(), impp.getHeight());
            impp.getProcessor().setLut(lut);
            impp.updateAndRepaintWindow();

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    protected void paintComponent(Graphics g) {
        // TODO Auto-generated method stub
        super.paintComponent(g);
    }

    public String getIconPath() {
        return this.pathToIcons;
    }

    void setIconPath(String iconPath) {
        this.pathToIcons = iconPath;
    }

    public JLabel makeReadback(String text, Dimension d) {

        JLabel label = new JLabel(text);
        label.setOpaque(true);
        label.setForeground(Color.WHITE);
        label.setBackground(Color.BLUE);
        
        Border pad = BorderFactory.createEmptyBorder(2, 10, 2, 2);
        label.setBorder(pad);
        
        if (d != null) {
            label.setPreferredSize(d);
        }

        return label;

    }
    
     public JTextField makeSetpoint( int l ) {

        JTextField set = new JTextField(l);
        set.setOpaque(true);
        set.setBackground(Color.CYAN);

        return set;

    }

    public boolean isCEBAFBCandMakeMonitor(CameraConfig cam) {
        if (cam == null) {
            return false;
        }

        if (cam.getId().equals(BC1) || cam.getId().equals(BC2) || cam.getId().equals(BC3) || cam.getId().equals(BC4)) {
            Log.log("makeMointor is true ", win.debug);
            return true;
        }

        return false;
    }

}
