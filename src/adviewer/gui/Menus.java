package adviewer.gui;

import ij.IJ;
import ij.ImageJ;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.io.FileSaver;
import ij.io.SaveDialog;
import ij.plugin.LutLoader;
import ij.plugin.StackWriter;
import ij.plugin.filter.AVI_Writer;
import ij.plugin.filter.Analyzer;
import ij.process.ImageProcessor;
import ij.process.LUT;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import adviewer.image.ImagePlusPlus;
import adviewer.image.ImageStream;
import adviewer.image.Stream;
import adviewer.util.LUTCollection;
import adviewer.util.Log;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ImageMath;
import java.awt.AWTEvent;

/**
 * Set of Static methods to return Menus
 *
 * @author Brian Freeman
 *
 */
public class Menus {

    public ImagePlusPlus impp;
    private ImageStream imageUpdater;
    private ADWindow win;

    public MenuItem start;
    public MenuItem stop;
    public MenuItem snap;

    public Menu capture;
    public MenuItem captureOn;
    public MenuItem captureOff;

    public ArrayList<CheckboxMenuItem> cmenuItems;
    public ArrayList<CheckboxMenuItem> fpsChecks;

    public IndexColorModel cm;
    public LUT lut;
    public LUT orginalLUT;
    public Toolbar tb;
    public ImageProcessor imageProcessor;

    public LUTCollection lc;

    public ImageJ ij;

    public boolean debug = true;

    public Menus() {
        init();
    }

    public Menus(ImagePlusPlus impp , LUTCollection luts) {
        //Log.log("Here in menus ................... " , debug);
        this.impp = impp;
        this.lc = luts;
        init();
    }
    
    public Menus(ImagePlusPlus impp) {
        //Log.log("Here in menus ................... " , debug);
        this.impp = impp;
        
        init();
    }

    public void init() {

        start = new MenuItem("Start...");
        stop = new MenuItem("Stop...");
        snap = new MenuItem("Snap...               (Ctrl + G)");

        capture = new Menu("Capture");
        captureOn = new MenuItem("ON");
        captureOff = new MenuItem("OFF");

        if (impp != null) {
            try {
                this.win = (ADWindow) impp.myWindow;

                orginalLUT = impp.getProcessor().getLut();
                imageProcessor = impp.getProcessor();

                if (this.win != null && this.win instanceof ADWindow && this.win.luts != null) {
                    lc = this.win.luts;
                    this.debug = this.win.debug;
                    //Log.log("ADWindow luts were found .... " , debug);
                } 
                else{
                        Log.log("Check lut collection in ADWWindow" ,debug);
                }
            } catch (Exception e) {
                Log.log("Problem with setting the LUT", debug);
                if (debug) {
                    e.printStackTrace();
                }
            }
        }

        tb = new Toolbar();
        ij = IJ.getInstance();

        //lc = new LUTCollection();
    }

    /**
     * getPath was copied from a private function in ij.io.FileSaver
     *
     * @param type
     * @param extension
     * @return
     */
    public String getPath(String type, String extension) {
        if (impp == null) {
            Log.log("ImagePlusPlus is null .... returning", debug);
            return null;
        }

        String name = impp.getTitle();
        SaveDialog sd = new SaveDialog("Save as " + type, name, extension);
        name = sd.getFileName();
        if (name == null) {
            return null;
        }
        String directory = sd.getDirectory();
        impp.startTiming();
        String path = directory + name;
        return path;
    }

    public Menu createMeasurementsMenu() {

        final Menu menu = new Menu("Measurements", true);

        //measurment menu
        MenuItem measure = new MenuItem("Measure...        (Ctrl+M)");

        measure.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                //IJ.runPlugIn(impp,"ij.plugin.filter.Analyzer" ,"Measure" );
                Analyzer analyzer = new Analyzer();
                analyzer.setup("", impp);

                //ResultsTable.createTableFromImage(impp.getProcessor()).show("This");
                analyzer.run(impp.getProcessor());
            }
        });
        menu.add(measure);

        MenuItem setMeasurments = new MenuItem("Set Measurements...");
        setMeasurments.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                IJ.runPlugIn(impp, "ij.plugin.filter.Analyzer", "set");

            }
        });
        menu.add(setMeasurments);

        MenuItem clear = new MenuItem("Clear Measurments...");
        clear.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                IJ.runPlugIn(impp, "ij.plugin.filter.Analyzer", "clear");

            }
        });
        menu.add(clear);
        menu.addSeparator();

        MenuItem setScale = new MenuItem("Set Sclae...");
        setScale.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                IJ.runPlugIn(impp, "ij.plugin.filter.ScaleDialog", null);

            }
        });
        menu.add(setScale);

        MenuItem calibrate = new MenuItem("Calibrate...");
        calibrate.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                IJ.runPlugIn(impp, "ij.plugin.filter.Calibrator", null);

            }
        });
        menu.add(calibrate);

        MenuItem hist = new MenuItem("Histogram...          (Ctrl+H)");
        hist.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                IJ.runPlugIn(impp, "ij.plugin.Histogram", "plot");

            }
        });
        menu.add(hist);

        MenuItem profiler = new MenuItem("Profiler (Horizontal)...     (Ctrl+K)");
        profiler.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                IJ.runPlugIn(impp, "ij.plugin.Profiler", "plot");

            }
        });
        menu.add(profiler);

        MenuItem profiler_y = new MenuItem("Profiler (Vertical)...");
        profiler_y.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                ProfilePlot pp = new ProfilePlot(impp, true);
                pp.getPlot().getProcessor().rotate(90);
                pp.getPlot().updateImage();
                pp.createWindow();
                //PlotWindow p = pp.getPlot().show();

                System.out.println("Y Profile....");
                //IJ.runPlugIn(impp, "ij.plugin.filter.Profiler" , "plot" );

            }
        });
        menu.add(profiler_y);

        return menu;

    }

    /**
     * Save As Menu
     *
     */
    public Menu createSaveAsMenu() {

        if (this.impp == null) {
            return null;
        }

        final Menu menu = new Menu("Save As", true);

        try {
            final FileSaver saver = new FileSaver(this.impp);
            //jpeg
            final MenuItem jpeg = new MenuItem("JPEG");

            jpeg.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    saver.saveAsJpeg();
                }
            });

            //add items
            menu.add(jpeg);

            //png
            final MenuItem png = new MenuItem("PNG");

            png.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    saver.saveAsPng();
                }
            });

            //add items
            menu.add(png);

            //text image
            final MenuItem gif = new MenuItem("GIF");

            gif.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    saver.saveAsGif();
                }
            });

            //add items
            menu.add(gif);

            //pgm
            final MenuItem pgm = new MenuItem("PGM");

            pgm.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    saver.saveAsPgm();
                }
            });

            //add items
            menu.add(pgm);

            //raw
            final MenuItem raw = new MenuItem("Raw");

            raw.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    saver.saveAsRaw();
                }
            });

            //add items
            menu.add(raw);

            //text image
            final MenuItem textImage = new MenuItem("Text Image");

            textImage.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    saver.saveAsText();
                }
            });

            //add items
            menu.add(textImage);

            //text image for Roi
            final MenuItem roiAsTextImage = new MenuItem("ROI as pgm");

            roiAsTextImage.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    String path = getPath("Text", ".pgm");
                    File file = new File(path);
                    try {

                        int[] pixVals = impp.getRoiAsIntArray();

                        PrintWriter writer = new PrintWriter(file);
                        if (!file.canWrite()) {
                            //writeToStatus("Somtehing went wrong .... ");
                            return;
                        } else {
                            Roi roi = impp.getRoi();
                            Rectangle r = roi.getBounds();
                            int i = 1;

                            Log.log("Pixel array length = " + pixVals.length, debug);
                            writer.println("P2");
                            writer.println((int) r.getWidth() + " " + (int) r.getHeight());
                            writer.println(255);
                            for (int j : pixVals) {

                                writer.print(j + " ");
                                if (i % r.getWidth() == 0) {
                                    writer.println();
                                    i = 0;
                                }
                                i++;

                            }

                        }

                        writer.close();

                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    //RoiEncoder re = new RoiEncoder(path);
                    //System.out.println(path);
                }
            });

            //add items
            menu.add(roiAsTextImage);

            menu.addSeparator();

            //stack menu
            final MenuItem imgseq = new MenuItem("Stack as Image Sequence");

            imgseq.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    StackWriter writer = new StackWriter();
                    writer.run("Image Sequence");
                }
            });
            //add items
            menu.add(imgseq);

            //stack menu
            final MenuItem avi = new MenuItem("Stack as AVI");

            avi.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent arg0) {
                    // TODO Auto-generated method stub
                    AVI_Writer writer = new AVI_Writer();
                    writer.setup("AVI", imageUpdater.impp);
                    writer.run(imageProcessor);
                }
            });
            //add items
            menu.add(avi);
        } catch (Exception e) {
            System.err.println("There was an issue creating the SaveAs Menu in adviewer.gui.Menus ..... line 452 \n " + e.getMessage());
        }
        return menu;
    }

    public void addImageUpdaterItems(Menu menu) {

        checkButtonState();

        start.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (impp.imageUpdater.getImagePanel() != null) {
                    impp.imageUpdater.show = true;
                    impp.imageUpdater.getImagePanel().timer.start();
                    stop.setEnabled(true);
                    snap.setEnabled(true);
                    start.setEnabled(false);
                }

            }
        });

        //stop 
        stop.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (impp.imageUpdater.getImagePanel() != null) {
                    impp.imageUpdater.show = false;
                    impp.imageUpdater.getImagePanel().timer.stop();

                    stop.setEnabled(false);
                    snap.setEnabled(false);
                    start.setEnabled(true);
                }
            }
        });

        //take snap shot
        snap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Date date = new Date();
                SimpleDateFormat simpleDate = new SimpleDateFormat("MM/dd/yy HH:mm:ss");

                if (impp.imageUpdater != null) {
                    impp.imageUpdater.makeImageCopy(impp.getTitle());
                }
            }
        });

        captureOn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {

                if (impp != null && impp.imageUpdater != null) {

                    impp.imageUpdater.isSaveToStack = true;
                    impp.imageUpdater.isNewStack = true;
                    impp.imageUpdater.captureOn();
                }

                Log.log("record on", debug);
            }
        });

        captureOff.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (impp.imageUpdater != null) {
                    impp.imageUpdater.captureOff();

                    impp.imageUpdater.show = false;
                    stop.setEnabled(false);
                    snap.setEnabled(false);
                    start.setEnabled(true);

                    Log.log("record off", debug);
                }
            }
        });

        capture.add(captureOn);
        capture.add(captureOff);

        menu.add(capture);
        menu.add(start);
        menu.add(stop);
        menu.add(snap);
        //menu.add(capture);
        //end controls

        menu.addSeparator();
    }

    public Menu createThrottleMenu() {
        Menu m = new Menu("Set Frame Rate ... ");
        fpsChecks = new ArrayList<CheckboxMenuItem>();

        final CheckboxMenuItem t1 = addFpsMenuItem("Full Frame Rate (Highest Bandwidth)", 0, fpsChecks);
        t1.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (CheckboxMenuItem cmi : fpsChecks) {
                        if (!cmi.equals(t1)) {
                            cmi.setState(false);
                        }
                    }
                    if (imageUpdater != null) {
                        imageUpdater.setSleepTime(imageUpdater.FULL_FPS);
                    }
                }
            }
        });
        fpsChecks.add(t1);
        m.add(t1);

        final CheckboxMenuItem t2 = addFpsMenuItem("60 FPS (High Bandwidth)", 1, fpsChecks);
        t2.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (CheckboxMenuItem cmi : fpsChecks) {
                        if (!cmi.equals(t2)) {
                            cmi.setState(false);
                        }
                    }
                    if (imageUpdater != null) {
                        imageUpdater.setSleepTime(imageUpdater.SIXTY_FPS);
                    }
                }
            }
        });
        fpsChecks.add(t2);
        m.add(t2);

        final CheckboxMenuItem t3 = addFpsMenuItem("30 FPS (Default)", 2, fpsChecks);
        t3.setState(true);
        t3.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (CheckboxMenuItem cmi : fpsChecks) {
                        if (!cmi.equals(t3)) {
                            cmi.setState(false);
                        }
                    }
                    if (imageUpdater != null) {
                        imageUpdater.setSleepTime(imageUpdater.TWENTY_FIVE_FPS);
                    }
                }
            }
        });
        fpsChecks.add(t3);
        m.add(t3);

        final CheckboxMenuItem t4 = addFpsMenuItem("15 FPS (Low Bandwidth)", 3, fpsChecks);

        t4.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (CheckboxMenuItem cmi : fpsChecks) {
                        if (!cmi.equals(t4)) {
                            cmi.setState(false);
                        }
                    }
                    if (imageUpdater != null) {
                        imageUpdater.setSleepTime(imageUpdater.FIFTEEN_FPS);
                    }
                }
            }
        });
        fpsChecks.add(t4);
        m.add(t4);

        final CheckboxMenuItem t5 = addFpsMenuItem("1 FPS (Lowest Bandwidth)", 0, fpsChecks);
        t5.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (CheckboxMenuItem cmi : fpsChecks) {
                        if (!cmi.equals(t5)) {
                            cmi.setState(false);
                        }
                    }
                    if (imageUpdater != null) {
                        imageUpdater.setSleepTime(imageUpdater.ONE_FPS);
                    }
                }
            }
        });
        fpsChecks.add(t4);
        m.add(t5);

        return m;

    }

    public void checkButtonState() {
        if (impp.imageUpdater != null) {
            if (!impp.imageUpdater.show) {
                stop.setEnabled(false);
                snap.setEnabled(false);
                start.setEnabled(true);
            } else {
                stop.setEnabled(true);
                snap.setEnabled(true);
                start.setEnabled(false);
            }
        }

    }

    public Menu createBackgroundSubtractionMenu() {
        Menu menu = new Menu("Background Subtraction ...");

        MenuItem save = new MenuItem("Save Background ... ");
        save.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent arg0) {
                // TODO Auto-generated method stub
                if (imageUpdater != null) {
                    imageUpdater.saveBackground();
                }
            }
        });

        menu.add(save);

        CheckboxMenuItem subtractionOn = new CheckboxMenuItem("Subtraction On ... ");
        subtractionOn.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    if (imageUpdater != null) {
                        imageUpdater.isSubtraction = true;
                    }
                } else {
                    if (imageUpdater != null) {
                        imageUpdater.isSubtraction = false;
                    }
                }
            }
        });

        menu.add(subtractionOn);

        return menu;
    }

    public Menu createImageManipMenu() {
        Menu menu = new Menu("Image Processing ... ", true);

        menu.add(createBackgroundSubtractionMenu());

        MenuItem gamma = new MenuItem("Gamma ... ");
        gamma.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                impp.gamma = getGammaValue(impp.gamma);
            }
        });

        menu.add(gamma);

        CheckboxMenuItem findEdges = new CheckboxMenuItem("Find Edges ... ");
        findEdges.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {

                    impp.findedges = true;
                    IJ.runPlugIn(impp, "ij.plugin.filter.Filters", "edge");

                } else {

                    impp.findedges = false;

                }
            }
        });
        menu.add(findEdges);

        return menu;
    }

    /**
     * Using a Generic Dialog (from ij.gui) to get and set the gamma value for
     * the ImagePlusPLus Object
     *
     * @param defaultValue is the value that will show in the dialog first
     * @return the double from slider that will set the gamma value
     */
    double getGammaValue(double defaultValue) {
        GenericDialog gd = new GenericDialog("Gamma"); // init the dialog
        gd.addSlider("Value:", 0.05, 5.0, impp.gamma); // make the slider
        gd.addDialogListener(new DialogListener() { //using the DialogListener Interface

            @Override
            public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
                if (!gd.wasCanceled()) {
                    impp.gamma = (double) gd.getNextNumber(); //if the slider changes then change the impp gamma value
                    return true;
                } else {
                    return false;
                }

            }
        });
        gd.showDialog(); //show the dilaog
        if (gd.wasCanceled()) {
            return defaultValue; //if the user canels then set the impp back to the initial value
        }
        return (double) gd.getNextNumber(); // return the number
    }

    /**
     * Add the Fps radio selctions to FPS Menu
     *
     * @param type of tool it is
     * @param index from ImageJ
     * @param cbmi - Array of all the items. adds it to the array. The function
     * will loop through the array of checkbox items, and will set all to false
     * if the
     * @return
     */
    public CheckboxMenuItem addFpsMenuItem(String type, final int index, final ArrayList<CheckboxMenuItem> cbmi) {
        final CheckboxMenuItem item = new CheckboxMenuItem(type);
        item.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (CheckboxMenuItem cmi : cbmi) {
                        if (!cmi.equals(item)) {
                            cmi.setState(false);
                        }
                    }
                }
            }
        });
        cbmi.add(item);
        return item;

    }

    /**
     * Basic set of ImageJ tools
     *
     * @return the tools menu
     */
    public Menu createToolMenu() {
        cmenuItems = new ArrayList<CheckboxMenuItem>();

        final Menu menu = new Menu("Tools", true);

        //magnifier, zoom in and out. I would like to add key bindings to this
        final CheckboxMenuItem mag = addToolMenuItem("Magnifer", Toolbar.MAGNIFIER, cmenuItems);
        menu.add(mag);

        //hand tool
        final CheckboxMenuItem hand = addToolMenuItem("Hand", Toolbar.HAND, cmenuItems);
        menu.add(hand);

        //rectangluar selection. Would be nice to match this to the ROI epics PV, and zoom in. 
        final CheckboxMenuItem rect = addToolMenuItem("Rectangle", Toolbar.RECTANGLE, cmenuItems);
        rect.setState(true);
        menu.add(rect);

        //line tool
        final CheckboxMenuItem line = addToolMenuItem("Line", Toolbar.LINE, cmenuItems);
        menu.add(line);

        //oval tool
        final CheckboxMenuItem oval = addToolMenuItem("Oval", Toolbar.OVAL, cmenuItems);
        menu.add(oval);

        //ellipse tool		
        //add and elliptical selection
        final CheckboxMenuItem ellipse = new CheckboxMenuItem("Ellispe");
        ellipse.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (CheckboxMenuItem cmi : cmenuItems) {
                        if (!cmi.equals(ellipse)) {
                            cmi.setState(false);
                        }
                    }

                    IJ.setTool("elliptical");
                }
            }
        });
        cmenuItems.add(ellipse);
        menu.add(ellipse);

        //freehand draw tool
        final CheckboxMenuItem freehand = addToolMenuItem("Free Hand", Toolbar.FREEROI, cmenuItems);
        menu.add(freehand);

        //angle tool
        final CheckboxMenuItem angle = addToolMenuItem("Angle", Toolbar.ANGLE, cmenuItems);
        menu.add(angle);

        //cross hairs
        final CheckboxMenuItem cross = addToolMenuItem("Crosshair", Toolbar.CROSSHAIR, cmenuItems);
        menu.add(cross);

        //poly line
        final CheckboxMenuItem polyline = addToolMenuItem("Polyline", Toolbar.POLYLINE, cmenuItems);
        menu.add(polyline);

        //point tool
        final CheckboxMenuItem point = addToolMenuItem("Point", Toolbar.POINT, cmenuItems);
        menu.add(point);

        return menu;
    }

    /**
     * Add the tool to Menu
     *
     * @param type of tool it is
     * @param index from ImageJ
     * @param cbmi - Array of all the items. adds it to the array. The function
     * will loop through the array of checkbox items, and will set all to false
     * if the
     * @return
     */
    public CheckboxMenuItem addToolMenuItem(String type, final int index, final ArrayList<CheckboxMenuItem> cbmi) {
        final CheckboxMenuItem item = new CheckboxMenuItem(type);
        item.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (CheckboxMenuItem cmi : cbmi) {
                        if (!cmi.equals(item)) {
                            cmi.setState(false);
                        }
                    }

                    IJ.setTool(index);
                }
            }
        });
        cbmi.add(item);
        return item;

    }

    /**
     * Mehtod that creates a menu of LUT choices. Only one is selectable at a
     * time
     *
     * @return Menu to add to popup menu
     */
    public Menu createLUTMenu() {

        //final LUTCollection lc = new LUTCollection();
        final ArrayList<CheckboxMenuItem> cmenuItems = new ArrayList<CheckboxMenuItem>();

        final Menu menu = new Menu("False Color Maps (LUTs)", true);

        final CheckboxMenuItem reset = new CheckboxMenuItem("Reset LUT");

        reset.addItemListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    for (CheckboxMenuItem cmi : cmenuItems) {
                        if (!cmi.equals(reset)) {
                            cmi.setState(false);
                        }
                    }

                    impp.getProcessor().setLut(orginalLUT);
                    impp.updateAndRepaintWindow();
                }
            }
        });
        cmenuItems.add(reset);
        menu.add(reset);

        menu.addSeparator();

        
        
        /* loop though lut collection Objects and then add the names*/
        //for (final String s : lc.getMap().keySet()) {
        for (final String s : lc.getSortedKeys()) {
            final CheckboxMenuItem item = new CheckboxMenuItem(s);
            //IJ.log("Adding LUT " + s);

            item.addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    if (e.getStateChange() == ItemEvent.SELECTED) {
                        for (CheckboxMenuItem cmi : cmenuItems) {
                            if (!cmi.equals(item)) {
                                cmi.setState(false);

                            }
                        }
                        changeLUT(lc.getMap().get(item.getLabel()));
                    }
                }
            });
            cmenuItems.add(item);
            menu.add(item);

        }

        return menu;
    }

    public void changeLUT(String path) {
        //IJ.run("Rainbow_RGB");
        Log.log(path , debug);
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

    public void setImageUpdater(ImageStream imageUpdater) {
        this.imageUpdater = imageUpdater;
    }

}
