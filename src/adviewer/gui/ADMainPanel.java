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
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
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
import java.net.URL;
import java.util.ArrayList;
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
    private String pathToIcons = "/root/workspace/java/adviewer/icons/";
    //public String pathToIcons = "/a/dvlcsue/dvlhome/apps/a/adViewer/dvl_2-0/src/adviewer/icons/";
    private int guiBuildNumber;
    private FileSaver saver;
    public String savePath;
    public String fileName;
    public int seqCount = 1;
    public boolean saveAsSeq = false;
    private Panel xPlotPanel;
    private Panel imagePanelContainer;
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

    public ADMainPanel(CameraConfig cam, ADWindow win, int guiBuildNumber) {
        super();
        this.win = win;
        this.cam = cam;
        init(guiBuildNumber);

    }

    public ADMainPanel(ImagePlusPlus impp, ADWindow win, int guiBuildNumber) {
        super();
        this.win = win;
        this.impp = impp;
        //this.win.impp = impp;
        init(guiBuildNumber);

    }

    public void setCams(CameraCollection cams) {
        this.cams = cams;
    }

    public void init(int guiBuildNumber) {
        layout = new BorderLayout(3, 3);
        this.setLayout(layout);
        layout.setHgap(3);

        componetList = new HashMap<String, Component>();

        this.guiBuildNumber = guiBuildNumber;

        if (impp != null) {
            imagePanel = new ImagePanel(impp, this.win);
        } else {

            Log.log("No ImagePlusPlus provided ... so trying stream", win.debug);

            if (cam == null) {

                Log.log("Please provide a Camera Config", win.debug);

                return;
            }
            imagePanel = new ImagePanel(cam, this.win); //this statement will block until it has an image

        }

        if (this.win != null) {
            this.win.impp = imagePanel.impp;
            this.impp = this.win.impp;
            this.ic = imagePanel.ic;

            this.cams = this.win.cams;
            this.imagePanel.setMainPanel(this); //set a reference in ImagePanel to this MainPanel 

           // imagePanelContainer = new Panel(new GribBagLayout);
            // imagePanelContainer.add(imagePanel);
            this.add(createImagePanelContainer(), BorderLayout.CENTER);
            // componetList.put("imagePanel", imagePanel);

            addComponents(guiBuildNumber);
        }

    }

    GridBagConstraints gc = new GridBagConstraints();

    public Component createImagePanelContainer() {
        imagePanelContainer = new Panel(new GridBagLayout());
        gc.weightx = gc.weighty = 1.0;
        gc.gridx = 1;
        gc.gridy = 0;

        imagePanelContainer.add(imagePanel, gc);

        return imagePanelContainer;
    }

    public void addComponents(int guiBuildNumber) {

        blackline = BorderFactory.createLineBorder(Color.black);
        raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
        loweredetched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
        raisedbevel = BorderFactory.createRaisedBevelBorder();
        loweredbevel = BorderFactory.createLoweredBevelBorder();
        empty = BorderFactory.createEmptyBorder();

        title = BorderFactory.createTitledBorder("title");

        try {
            if (guiBuildNumber == JUSTVIEWER) {
                return;
            }
            if (guiBuildNumber > JUSTVIEWER) {
                isEastShowing = false;
                this.add(createNorth(), BorderLayout.NORTH);
                if (imagePanel != null) {
                    if (imagePanel.streamer != null && imagePanel.streamer.isCEBAFBC) {
                        this.add(createSouth(), BorderLayout.SOUTH);
                    }
                }

            }
            if (guiBuildNumber > VIEWERWSTATUS) {
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
        statusbar1 = new Panel(new BorderLayout(2, 2));
        JPanel p = new JPanel();

        fps = new JLabel(" 0.00 fps ");
        fps.setPreferredSize(new Dimension(100, 20));
        fps.setAlignmentX(RIGHT_ALIGNMENT);
        fps.setAlignmentY(RIGHT_ALIGNMENT);
        fps.setForeground(Color.WHITE);

        pixelInspector = new JLabel("Pixel Values and Cursor Position");
        pixelInspector.setAlignmentX(RIGHT_ALIGNMENT);
        pixelInspector.setPreferredSize(new Dimension(200, 20));
        pixelInspector.setAlignmentY(RIGHT_ALIGNMENT);
        pixelInspector.setForeground(Color.WHITE);

        p.add(pixelInspector);

        ImageIcon right = createImageIcon(pathToIcons + "plus_black_small.png", "Button1");
        ImageIcon left = createImageIcon(pathToIcons + "minus_black_small.png", "Button1T");

        ButtonIcon eastButton = null;

        if (this.guiBuildNumber > VIEWERWSTATUS) {
            eastButton = new ButtonIcon(left, right);

        } else {
            eastButton = new ButtonIcon(right, left);

        }
        JPanel buttons = new JPanel(new BorderLayout());

        buttons.add(fps);
        if (eastButton != null) {
            eastButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent ae) {

                    toggleEastPanel();
                }
            });
            eastButton.setPreferredSize(new Dimension(20, 20));
            buttons.add(eastButton, BorderLayout.EAST);
        }

        p.setBackground(Color.BLUE);
        buttons.setBackground(Color.BLUE);

        statusbar1.add(p, BorderLayout.WEST);
        statusbar1.add(buttons, BorderLayout.EAST);

        return statusbar1;
    }

    public Component createNorth() throws IOException {
        northPanel = new Panel(new BorderLayout(2, 2));

        northPanel.add(createStatusBar1(), BorderLayout.SOUTH);
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
            Log.log("Image Icon created .... " + description, win.debug);
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
        Dimension d = null;

        eastPanel.setFocusable(false);

        JTabbedPane tabs = new JTabbedPane();

        tabs.add(createImagePanel(), "Image");
        tabs.add(createToolsPanel(), "Tools");
        tabs.add(createPlotPanel(), "Plots");
        tabs.add(createSavePanel(), "Save");

        
        tabs.setSelectedIndex(2);

        eastPanel.add(tabs);

        
        return eastPanel;
    }

    public Component createXPlotPanel() {
        xPlotPanel = new Panel();

        JTabbedPane tabs = new JTabbedPane();

        //tabs.setPreferredSize(new Dimension( impp.getWidth() , impp.getHeight()));
        tabs.add(createXPlot(), "xplot");

        xPlotPanel.add(tabs);

        return xPlotPanel;
    }

    public Component createYPlotPanel() {
        yPlotPanel = new Panel();

        JTabbedPane tabs = new JTabbedPane();

        //tabs.setPreferredSize(new Dimension( impp.getWidth() , impp.getHeight()));
        tabs.add(createYPlot(), "yplot");

        yPlotPanel.add(tabs);

        return yPlotPanel;
    }

    public Component createXPlot() {
        Panel panel = new Panel();

        boolean isY = false;
        boolean showFit = false;

        xp = Helpers.getPlotPlus(impp, isY, showFit, win.debug);
        xpp = new PlotPanel(xp, impp, isY, win.debug);
        xpp.setMainPanel(this); //set a refernce to this panel in PlotPanel

        //PlotCanvas pc = new PlotCanvas(impp);
        //pc.setPlot(xp);
        panel.add(xpp);

        return panel;
    }

    public Component createYPlot() {
        Panel panel = new Panel();

        boolean isY = true;
        boolean showFit = false;

        yp = Helpers.getPlotPlus(impp, isY, showFit, win.debug);
        ypp = new PlotPanel(yp, impp, isY, win.debug);
        ypp.setMainPanel(this); //set a refernce to this panel in PlotPanel

        //PlotCanvas pc = new PlotCanvas(impp);
        //pc.setPlot(xp);
        //ypp.imp.getProcessor().rotate(90.0);
        panel.add(ypp);

        return panel;
    }

    public Component createImagePanel() {
        Panel panel = new Panel(new GridBagLayout());
        // panel.setPreferredSize(new Dimension(200 , 30) );
        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints gc = new GridBagConstraints();

        gc.weightx =1.0;
        gc.weighty = 1.0;
        gc.fill = GridBagConstraints.BOTH;
        
        JPanel p = new JPanel();
        p.setLayout(layout);

        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;

        p.add(createBackGroundSubtractPanel(), gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.anchor = GridBagConstraints.WEST;

        p.add(createLutDropDownPanel(), gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.anchor = GridBagConstraints.WEST;

        p.add(createGammaAdjust(), gc);

        gc.gridx = 0;
        gc.gridy = 3;
        gc.anchor = GridBagConstraints.WEST;

        p.add(createBrightnessAdjust(), gc);

        // gc.gridx = 0;
        //gc.gridy = 0;
        //gc.anchor = GridBagConstraints.WEST;
        // p.add(createFindEdgesPanel(), gc);
        
        gc.gridx = 0;
        gc.gridy = 0;
        panel.add(p, gc);
        return panel;
    }

    public Component createTestButton() {
        Panel panel = new Panel();

        JPanel p = new JPanel();

        JButton b = new JButton("Test Out Things");
        b.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

               //toggleXPlotPanel();
                //Roi roi = new Roi(70, 70, 120, 120);
                // Line line = new Line(impp.getWidth() / 2, impp.getHeight() - 1, impp.getWidth() / 2, 0);
                // TextRoi text = new TextRoi(20, 20, "test");
                //impp.setRoi(text);
                //impp.saveRoi();
                //impp.setRoi(roi);
                //Overlay ov = new Overlay(roi);
                //impp.setOverlay(ov);
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
        p.add(b1);

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
        p.add(b1);

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
        
        JLabel unitsLabel =  new JLabel("Units :   ");
        unitsVal  = new JLabel( this.impp.getCalibration().getUnits() );
        
       p.add(unitsLabel);
       p.add(unitsVal);
        
        title = BorderFactory.createTitledBorder("Units");
        p.setBorder(title);
        panel.add(p);
        return panel;
    }
    
    public Component createXFitsPanel() {

        Panel panel = new Panel();

        JPanel p = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.weightx = gc.weighty = 1.0;
        
        String sigma = "\u03A3";
        Dimension d = new Dimension(70, 20);

        JLabel min = new JLabel("Y Min:      ");
        minXVal = new JLabel("min   ");
        minXVal.setPreferredSize(d);
        JLabel max = new JLabel("Y Max:      ");
        maxXVal = new JLabel("max   ");
        max.setPreferredSize(d);
        JLabel centroid = new JLabel("Centroid:   ");
        centroidXVal = new JLabel("cent  ");
        centroidXVal.setPreferredSize(d);
        JLabel rms = new JLabel(sigma.toLowerCase() + ":        ");
        rmsXVal = new JLabel("rms     ");
        rmsXVal.setPreferredSize(d);
        JLabel twoRms = new JLabel("2*" + sigma.toLowerCase() + ":        ");
        twoRmsXVal = new JLabel("2*rms ");
        twoRmsXVal.setPreferredSize(d);
        JLabel fwhmx = new JLabel("FWHMx:        ");
        fwhmXVal = new JLabel("FWHMx ");
        fwhmXVal.setPreferredSize(d);
        
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

        String sigma = "\u03A3";
        Dimension d = new Dimension(70, 20);
        
        JLabel min = new JLabel("X Min:      ");
        minYVal = new JLabel("min                ");
        minYVal.setPreferredSize(d);
        JLabel max = new JLabel("X Max:      ");
        maxYVal = new JLabel("max                ");
        maxYVal.setPreferredSize(d);
        JLabel centroid = new JLabel("Centroid:   ");
        centroidYVal = new JLabel("cent               ");
        centroidYVal.setPreferredSize(d);
        JLabel rms = new JLabel(sigma.toLowerCase() + ":        ");
        rmsYVal = new JLabel("rms                ");
        rmsYVal.setPreferredSize(d);
        JLabel twoRms = new JLabel("2*" + sigma.toLowerCase() + ":        ");
        twoRmsYVal = new JLabel("2*sigma            ");
        twoRmsYVal.setPreferredSize(d);
        JLabel fwhmy = new JLabel("FWHMy:        ");
        fwhmYVal = new JLabel("FWHMy             ");
        fwhmYVal.setPreferredSize(d);

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

        p.add(fwhmy, gc);

        gc.gridx = 0;
        gc.gridy = 4;

        p.add(min, gc);

        gc.gridx = 0;
        gc.gridy = 5;

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
        return panel;

    }

    public Component createToolsPanel() {
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

        p.add(createShapeTools(), gc);

        gc.gridx = 0;
        gc.gridy = 1;
        gc.anchor = GridBagConstraints.WEST;

        p.add(createZoomTools(), gc);

        gc.gridx = 0;
        gc.gridy = 2;
        gc.anchor = GridBagConstraints.WEST;

        p.add(createTestButton(), gc);
        
        gc.gridx = 0;
        gc.gridy = 0;
        panel.add(p, gc);
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

        p.add(createToolButton(pathToIcons + "black_rectangle_small.png", Toolbar.RECTANGLE));
        p.add(createToolButton(pathToIcons + "oval_black.png", "elliptical"));
        p.add(createToolButton(pathToIcons + "polygon_small_2.png", Toolbar.FREEROI));

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

        p.add(createToolButton(pathToIcons + "hand_small.png", Toolbar.HAND));
        p.add(createToolButton(pathToIcons + "mag_small.png", Toolbar.MAGNIFIER));

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

        p.add(createToolButton(pathToIcons + "line_small.png", Toolbar.LINE));
        p.add(createToolButton(pathToIcons + "multiLine_small.png", Toolbar.POLYLINE));
        p.add(createToolButton(pathToIcons + "angle_small.png", Toolbar.ANGLE));
        p.add(createToolButton(pathToIcons + "polygon_small.png", Toolbar.FREELINE));

        title = BorderFactory.createTitledBorder("Line");
        p.setBorder(title);
        panel.add(p);
        panel.setFocusable(false);
        return panel;

    }

    public Component createPointTools() {

        Panel panel = new Panel();

        JPanel p = new JPanel();

        p.add(createToolButton(pathToIcons + "point_small.png", Toolbar.POINT));
        p.add(createToolButton(pathToIcons + "crosshair_small.png", Toolbar.CROSSHAIR));

        title = BorderFactory.createTitledBorder("Point");
        p.setBorder(title);
        panel.add(p);
        panel.setFocusable(false);
        return panel;

    }
    public ArrayList<JButton> toolButtons = new ArrayList<JButton>();

    public JButton createToolButton(String iconPath, final int toolId) {

        final JButton b = new JButton();
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
            }
        });

        if (toolId == Toolbar.getToolId()) { //if the tool is equal to the default then set it to grren
            b.setBackground(Color.GREEN);
        }
        //add to list of tools
        toolButtons.add(b);
        return b;

    }

    public JButton createToolButton(String iconPath, final String toolId) {

        final JButton b = new JButton();
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
            }
        });
        if (toolId.equals(IJ.getToolName())) {
            b.setBackground(Color.GREEN);
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
        JPanel p = new JPanel();
        final Dimension d = new Dimension(28, 25);

        final JButton b1 = new JButton();
        b1.setIcon(createImageIcon(pathToIcons + "play_blue_small.png", "play"));
        if (imagePanel != null) {
            if (imagePanel.timer != null && imagePanel.timer.isRunning()) {
                b1.setBackground(Color.GREEN);
            }
        }

        final JButton b2 = new JButton();
        b2.setIcon(createImageIcon(pathToIcons + "pause_blue_small.png", "pause"));

        final JButton b3 = new JButton();
        b3.setIcon(createImageIcon(pathToIcons + "black_cam_small.png", "snap"));

        //JButton b3 = new JButton();
        //b3.setIcon( createImageIcon(pathToIcons + "play_blue_small.png", "play") );
        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (imagePanel != null) {
                    if (!imagePanel.timer.isRunning()) {
                        imagePanel.timer.start();
                    }
                    b1.setBackground(Color.GREEN);
                    b2.setBackground(null);

                }
            }
        });

        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (imagePanel != null) {
                    if (imagePanel.timer.isRunning()) {
                        imagePanel.timer.stop();
                    }
                    b1.setBackground(null);
                    b2.setBackground(Color.GREEN);
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

        b1.setPreferredSize(d);
        b2.setPreferredSize(d);
        b3.setPreferredSize(d);

        p.add(b1);
        p.add(b2);
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

        final JButton b1 = new JButton("Save");

        final JButton b2 = new JButton("On");

        final JButton b3 = new JButton("Reset");

        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (impp.imageUpdater != null) {
                    impp.imageUpdater.saveBackground();
                    b1.setBackground(Color.yellow);
                    b1.setText("Saved");
                }
            }
        });

        b2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (impp.imageUpdater != null) {
                    if (impp.imageUpdater.isSubtraction) {
                        impp.imageUpdater.isSubtraction = false;
                        b2.setBackground(null);
                        //b2.setText("Off");
                    } else {
                        impp.imageUpdater.isSubtraction = true;
                        b2.setBackground(Color.GREEN);
                        //b2.setText("On");
                    }

                }

            }
        });

        b3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (impp.imageUpdater != null) {

                    impp.imageUpdater.pixelBytes = null;
                    b1.setBackground(null);
                    b2.setSelected(false);
                    impp.imageUpdater.isSubtraction = false;
                    b2.setBackground(null);
                    b1.setText("Save");
                    // b2.setText("Off");
                }
            }
        });

        p.add(b1);
        p.add(b2);
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

        final JButton b1 = new JButton("Off");

        b1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                if (impp != null) {
                    if (impp.findedges) {
                        impp.findedges = false;
                        b1.setBackground(null);
                        b1.setText("Off");
                        if (impp.imageUpdater == null) {
                            //impp.resetPixels();
                            Log.log("reset ", win.debug);
                        }
                    } else {
                        impp.findedges = true;
                        b1.setBackground(Color.GREEN);
                        b1.setText("On");
                        if (impp.imageUpdater == null) {
                            IJ.runPlugIn(impp, "ij.plugin.filter.Filters", "edge");
                        }
                    }

                }

            }
        });

        p.add(b1);

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

        final JTextField text = new JTextField(15);
        //text.setText(savePath);
        JLabel label = new JLabel("Save Path: ");
        final JTextField text2 = new JTextField(15);
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

        p1.add(b1);
        p1.add(b2);
        p1.add(b3);
        p2.add(b4);
        p2.add(b5);
        p2.add(b6);
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
                    Helpers.saveRoiAsDat(impp, null, false, win.debug);
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
        title = BorderFactory.createTitledBorder("Save ROI As");
        p.setBorder(title);
        panel.setFocusable(false);
        panel.add(p);
        return panel;
    }

    public Component createLutDropDownPanel() {

        Panel panel = new Panel();
        // panel.setPreferredSize(new Dimension(200 , 30) );
        JPanel p = new JPanel(new FlowLayout(2));
        if (impp != null) {
            originalLut = impp.getProcessor().getLut();
        }

        final Choice chooser = new Choice();
        chooser.addItem("None");

        for (String s : win.luts.getSortedKeys()) {
            chooser.add(s);

        }

        chooser.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {

                if (chooser.getSelectedItem().equals("None")) {
                    impp.getProcessor().setLut(originalLut);
                } else {
                    changeLUT(win.luts.map.get(chooser.getSelectedItem()));
                }
            }
        });

        JButton resetLut = new JButton("Reset");
        resetLut.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                impp.getProcessor().setLut(originalLut);
                chooser.select("None");
            }
        });

        title = BorderFactory.createTitledBorder("Look Up Tables (LUT)");
        p.setBorder(title);

        p.add(chooser);
        p.add(resetLut);
        panel.setFocusable(false);
        panel.add(p);
        return panel;

    }

    public Component createCamDropDown() {
        dropPanel = new Panel(new BorderLayout(5, 3));

        JButton button = new JButton("Test");
        dropPanel.add(button, BorderLayout.WEST);

        if (cams == null) {
            return dropPanel;
        }

        final Choice chooser = new Choice();
        String[] ids = new String[cams.map.size()];
        int i = 0;
        for (CameraConfig c : cams.cameras) {
            ids[i] = c.getId();

            if (ids[i] == null) {
                continue;
            } else {
                chooser.add(ids[i]);
            }
            i++;
            // Log.log( c.getId() , win.debug);
        }

        //drop = new JComboBox( ids );
        chooser.select(cam.getId());
        chooser.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent ie) {
                Log.log(cams.map.get(chooser.getSelectedItem()).getId() + " was selected", win.debug);

                cam = cams.map.get(cams.map.get(chooser.getSelectedItem()).getId()); //main panel cam
                win.cam = cam; //set ADWindow cam
                imagePanel.cam = cam; //update imagepanel.cam

                imagePanel.streamChanged = true;

            }
        });
        dropPanel.add(chooser, BorderLayout.EAST);

        //if(cams == null) return drop;
        //for( int i = 0 ; i < cams.map.size() ; i++){
        //  drop.addItem(cams.map.get(i).getId());
        //}
        Log.log(" Cam Drop Down Done....  ", win.debug);
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

        final Panel panel = new Panel(new BorderLayout(2, 2));
        Dimension d2 = new Dimension((300), 27);

        final String defaultString = "Check to get Inserted Viewer";
        messageLabel = new JLabel(defaultString);
        messageLabel.setPreferredSize(d2);
        messageLabel.setAlignmentX(CENTER_ALIGNMENT);
        messageLabel.setAlignmentY(CENTER_ALIGNMENT);
        //messageLabel.setBorder(blackline);
        messageLabel.setForeground(Color.WHITE);

        final JCheckBox getViewerSigmas = new JCheckBox();

        final Panel labels = new Panel(new GridLayout(6, 2));

        final JLabel label1 = new JLabel("Viewer:");
        final JLabel label2 = new JLabel("SigmaX:");
        final JLabel label3 = new JLabel("SigmaY:");
        final JLabel label4 = new JLabel("Aspect Ratio:");
        final JLabel label5 = new JLabel("ITVXXXX");
        final JLabel label6 = new JLabel("1.023");
        final JLabel label7 = new JLabel("2.345");
        final JLabel label8 = new JLabel("1:1.2");

        label1.setForeground(Color.WHITE);
        label2.setForeground(Color.WHITE);
        label3.setForeground(Color.WHITE);
        label4.setForeground(Color.WHITE);
        label5.setForeground(Color.WHITE);
        label6.setForeground(Color.WHITE);
        label7.setForeground(Color.WHITE);
        label8.setForeground(Color.WHITE);

        labels.add(label1);
        labels.add(label5);
        labels.add(label2);
        labels.add(label6);
        labels.add(label3);
        labels.add(label7);
        labels.add(label4);
        labels.add(label8);

        final RunSystemCommand run = new RunSystemCommand(win.debug, 50);
        run.setLabels(labels, label5, label6, label7, label8);
        run.messageLabel = messageLabel;

        getViewerSigmas.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {

                if (getViewerSigmas.isSelected()) {
                    run.run = true;
                    new Thread(run).start();
                    panel.add(labels, BorderLayout.SOUTH);
                    messageLabel.setText("Building viewer list.... takes ~1 min ");

                } else {
                    run.run = false;
                    messageLabel.setText(defaultString);
                    panel.remove(labels);

                }
                panel.repaint();
                win.pack();

            }
        });

        panel.setBackground(Color.BLUE);

        panel.add(messageLabel, BorderLayout.WEST);
        panel.add(getViewerSigmas, BorderLayout.EAST);
        return panel;
    }

    public Component createSouth() {
        southPanel = new Panel(new BorderLayout(2, 2));

        //south.setBackground(Color.BLUE);
        southPanel.add(createMessageLabel(), BorderLayout.SOUTH);

        return southPanel;
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

    public String getIconPath(){
        return this.pathToIcons;    
    }
    
    void setIconPath(String iconPath) {
        this.pathToIcons = iconPath;
    }
}
