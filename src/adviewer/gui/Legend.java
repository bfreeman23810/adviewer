/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.gui;

import adviewer.util.Log;
import ij.*;
import ij.plugin.filter.PlugInFilter;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import java.awt.datatransfer.*;
import ij.gui.*;
import ij.process.*;
import ij.measure.Measurements;
import ij.plugin.filter.Analyzer;
import static ij.plugin.filter.PlugInFilter.DOES_ALL;
import static ij.plugin.filter.PlugInFilter.DOES_RGB;
import static ij.plugin.filter.PlugInFilter.NO_CHANGES;
import ij.text.TextWindow;

/**
 *
 * @author root
 */
public class Legend implements Measurements, ActionListener {

    /**
     * This plugin displays a legend for an ImageJ image. Bob Dougherty,
     * OptiNav, Inc., 4/14/2002 Based largely on HistogramWindow.java by Wayne
     * Rasband. Version 0 4/14/2002 Version 1 6/14/2002 Many revisions,
     * including addition of the legend to the image. Version 2 6/15/2002
     * Control over colors, including none. Automatic box width. Version 3
     * 6/17/2002 0 decimal places by default. Option for manual box width.
     * Version 3.1 10/6/2004 Updated for ImageJ 1.32
     */

    /*	License:
     Copyright (c) 2002, 2005, OptiNav, Inc.
     All rights reserved.

     Redistribution and use in source and binary forms, with or without
     modification, are permitted provided that the following conditions
     are met:

     Redistributions of source code must retain the above copyright
     notice, this list of conditions and the following disclaimer.
     Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in the
     documentation and/or other materials provided with the distribution.
     Neither the name of OptiNav, Inc. nor the names of its contributors
     may be used to endorse or promote products derived from this software
     without specific prior written permission.

     THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
     "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
     LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
     A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
     CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
     EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
     PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
     PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
     LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
     NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
     SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
     */
    final static int BAR_LENGTH = 128;
    final static int BAR_THICKNESS = 12;
    final static int XMARGIN = 12;
    final static int YMARGIN = 12;
    final static int INIT_WIN_WIDTH = 100;
    final static int WIN_HEIGHT = BAR_LENGTH + 2 * YMARGIN;
    final static int INSERT_PAD = 3;
    final static int BOX_PAD = 0;
    final static int LINE_WIDTH = 2;
    public static int nBins = 256;
    public static final String[] colors = {"White", "Black", "Red", "Green", "Blue", "Yellow", "None"};

    protected ImageStatistics stats;
    protected float[] cTable;
    protected int[] histogram;
    protected LookUpTable lut;
    protected Image img;
    protected Button setup, redraw, insert, unInsert;
    protected Checkbox ne, nw, se, sw;
    protected CheckboxGroup locGroup;
    protected Label value, note;
    //protected int decimalPlaces = -1;
    protected int decimalPlaces = 0;
    protected int newMaxCount;
    protected boolean logScale;
    public int numLabels = 5;
    public int fontSize = 12;
    public int win_width = INIT_WIN_WIDTH;
    protected int fontHeight = 0;
    public boolean boldText = false;
    public boolean autoWidth = true;
    protected Object backupPixels;
    protected byte[] byteStorage;
    protected int[] intStorage;
    protected short[] shortStorage;
    protected float[] floatStorage;
    public String fillColor = colors[0];
    public String textColor = colors[1];
    public String boxOutlineColor = colors[1];
    public String barOutlineColor = colors[1];

    protected ImagePlus impData;

    ImagePlus imp;
    public boolean debug;

    public int setup(String arg, ImagePlus imp) {
        this.imp = imp;
        return DOES_ALL - DOES_RGB + NO_CHANGES;
    }

    //public void run(ImageProcessor ip) {
      //  final Legend lw = new Legend(imp);
    //}

    /**
     * Displays a legend using the title "Legend of ImageName".
     */
    public Legend(ImagePlus imp, boolean debug) {
        //super(new ImagePlus("Legend for " + imp.getShortTitle(), GUI.createBlankImage(INIT_WIN_WIDTH, WIN_HEIGHT)));
        //setup();
        impData = imp;
        this.imp = imp;
        this.debug = debug;
        stats = imp.getStatistics(MIN_MAX, nBins);
        histogram = stats.histogram;
        lut = imp.createLut();
        cTable = imp.getCalibration().getCTable();
        try{ 
           // showLegend(imp, this.imp, 0, 0);
            verticalColorBar(imp.getProcessor(), imp.getWidth()-10, imp.getHeight()-10, 10, imp.getHeight()-20);
        }
        catch(Exception e ){
            if(debug) e.printStackTrace();
        }
    }

    public void showLegend(ImagePlus imp, ImagePlus destImp, int x, int y) {
       
        stats = imp.getStatistics(MIN_MAX, nBins);
        histogram = stats.histogram;
        lut = imp.createLut();
        cTable = imp.getCalibration().getCTable();

        
        ImageProcessor ip = destImp.getProcessor();

        Log.log(" processor done"  , debug);
        int maxTextWidth = drawText(ip, 0, 0, false);
        
        //if (autoWidth) {
           // win_width = XMARGIN + 5 + BAR_THICKNESS + maxTextWidth + XMARGIN;
        //}
        
        Log.log(" win width =" + win_width , true);
        Color c = getColor(fillColor);
        
        //if (c != null) {
          //  ip.setColor(c);
            //ip.setRoi(x, y, win_width, WIN_HEIGHT);
            //ip.fill();
        //}
        
        //ip.resetRoi();

        //drawLegend(ip, x, y);
        //destImp.updateAndDraw();

        //ip.setColor(Color.black);
        
        Log.log("show legend done",true);

    }

    public void clearLegend(ImagePlus destImp) {
        ImageProcessor ip = destImp.getProcessor();
        ip.setColor(Color.white);
        ip.setRoi(0, 0, INIT_WIN_WIDTH, WIN_HEIGHT);
        ip.fill();
        ip.resetRoi();
    }

    public void setup() {

        //setLayout(new GridLayout(3,1,3,3);
        value = new Label("          "); //10
        value.setFont(new Font("Monospaced", Font.PLAIN, 12));
        //add(value);

        setup = new Button("Setup");
        setup.addActionListener(this);
        //add(setup);

        insert = new Button("Insert");
        insert.addActionListener(this);
        //add(insert);

        //Insert location controls
        Panel location = new Panel(new GridLayout(2, 2));
        locGroup = new CheckboxGroup();

        nw = new Checkbox("NW");
        nw.setCheckboxGroup(locGroup);
        location.add(nw);

        ne = new Checkbox("NE");
        ne.setCheckboxGroup(locGroup);
        location.add(ne);

        sw = new Checkbox("SW");
        sw.setCheckboxGroup(locGroup);
        location.add(sw);

        se = new Checkbox("SE");
        se.setCheckboxGroup(locGroup);
        location.add(se);

        ne.setState(true);

        //add(location);

        note = new Label("(or selection)");
        note.setFont(new Font("Monospaced", Font.PLAIN, 10));
        //add(note);

        unInsert = new Button("UnInsert");
        unInsert.addActionListener(this);
        //add(unInsert);

        redraw = new Button("Redraw");
        redraw.addActionListener(this);
        //add(redraw);

        //pack();
    }

    public void mouseMoved(int x, int y) {
        if (value == null) {
            return;
        }
        int barPosition = WIN_HEIGHT - YMARGIN - 1 - y;
        if ((barPosition >= 0) && (barPosition <= BAR_LENGTH)) {
            double v = grayValue(barPosition);
            if (v == (int) v) {
                value.setText("  " + String.valueOf((int) v));
            } else {
                value.setText("  " + IJ.d2s(v, decimalPlaces));
            }
        } else {
            value.setText("");
        }
    }

    public double grayValue(int barPosition) {
        int x = barPosition;
        //if (x < 0) x = 0;
        //if (x > 255) x = 255;
        double index = (x * ((double) histogram.length) / BAR_LENGTH);
        int ind = (int) index;
        double v = 0.0;
        if (cTable != null && cTable.length == 256) {
            if (ind < 0) {
                v = cTable[0];
            } else if (ind > 255) {
                v = cTable[255];
            } else {
                v = cTable[(int) index];
            }
        } else if (cTable != null && cTable.length == 65536) {
            //int index2 = (int)(stats.histMin+index*stats.binSize+stats.binSize/2.0);
            int index2 = (int) (stats.histMin + index * stats.binSize);
            if (index2 >= 0 && index < 65536) {
                v = cTable[index2];
            }
        } else {
            v = stats.histMin + index * stats.binSize;
            //if (stats.binSize!=1.0) v += stats.binSize/2.0;
        }
        return v;
    }

    public void verticalColorBar(ImageProcessor ip, int x, int y, int thickness, int length) {
        int width = thickness;
        int height = length;
        byte[] rLUT, gLUT, bLUT;
        int mapSize = 0;
        //lut =  ip;
        
        java.awt.image.ColorModel cm = lut.getColorModel();
        if (cm instanceof IndexColorModel) {
            IndexColorModel m = (IndexColorModel) cm;
            mapSize = m.getMapSize();
            rLUT = new byte[mapSize];
            gLUT = new byte[mapSize];
            bLUT = new byte[mapSize];
            m.getReds(rLUT);
            m.getGreens(gLUT);
            m.getBlues(bLUT);
        } else {
            mapSize = 256;
            rLUT = new byte[mapSize];
            gLUT = new byte[mapSize];
            bLUT = new byte[mapSize];
            for (int i = 0; i < mapSize; i++) {
                rLUT[i] = (byte) i;
                gLUT[i] = (byte) i;
                bLUT[i] = (byte) i;
            }
        }
        for (int i = 0; i < BAR_LENGTH; i++) {
            int iMap = (int) Math.round(i * ((double) mapSize) / BAR_LENGTH);
            ip.setColor(new Color(rLUT[iMap] & 0xff, gLUT[iMap] & 0xff, bLUT[iMap] & 0xff));
            int j = BAR_LENGTH - i - 1;
            ip.moveTo(x, j + y);
            ip.lineTo(thickness + x, j + y);
        }

        Color c = getColor(barOutlineColor);
        if (c != null) {
            ip.setColor(c);
            ip.moveTo(x, y);
            ip.lineTo(x + width, y);
            ip.lineTo(x + width, y + height);
            ip.lineTo(x, y + height);
            ip.lineTo(x, y);
        }
    }

    
    
    protected void drawLegend(ImageProcessor ip, int xOffset, int yOffset) {
        int x, y;

        ip.setColor(Color.black);
        if (decimalPlaces == -1) {
            decimalPlaces = Analyzer.getPrecision();
        }
        x = XMARGIN + xOffset;
        y = YMARGIN + yOffset;

        verticalColorBar(ip, x, y, BAR_THICKNESS, BAR_LENGTH);

        drawText(ip, x + BAR_THICKNESS, y, true);

        Color c = getColor(boxOutlineColor);
        if (c != null) {
            ip.setColor(c);
            ip.setLineWidth(LINE_WIDTH);
            ip.moveTo(xOffset + BOX_PAD, yOffset + BOX_PAD);
            ip.lineTo(xOffset + win_width - BOX_PAD, yOffset + BOX_PAD);
            ip.lineTo(xOffset + win_width - BOX_PAD, yOffset + WIN_HEIGHT - BOX_PAD);
            ip.lineTo(xOffset + BOX_PAD, yOffset + WIN_HEIGHT - BOX_PAD);
            ip.lineTo(xOffset + BOX_PAD, yOffset + BOX_PAD);
        }

    }

    int drawText(ImageProcessor ip, int x, int y, boolean active) {

        Color c = getColor(textColor);
        if (c == null) {
            return 0;
        }
        ip.setColor(c);

        double hmin = stats.histMin;
        double hmax = stats.histMax;
        if (cTable != null && (int) hmin >= 0 && (int) hmax < cTable.length) {
            hmin = cTable[(int) hmin];
            hmax = cTable[(int) hmax];
        }
        double barStep = BAR_LENGTH;
        if (numLabels > 2) {
            barStep /= (numLabels - 1);
        }

        int fontType = boldText ? Font.BOLD : Font.PLAIN;
        Font font = new Font("SansSerif", fontType, fontSize);
        ip.setFont(font);

        int maxLength = 0;

        //Blank offscreen image for font metrics
        Image img = GUI.createBlankImage(128, 64);
        Graphics g = img.getGraphics();
        FontMetrics metrics = g.getFontMetrics(font);
        fontHeight = metrics.getHeight();

        for (int i = 0; i < numLabels; i++) {
            //double yLabelD = y + BAR_LENGTH - i*barStep - 1;
            double yLabelD = YMARGIN + BAR_LENGTH - i * barStep - 1;
            int yLabel = (int) Math.round(y + BAR_LENGTH - i * barStep - 1);
            //Linear interpolation to obtain labels that are not actual table colors
            int yL2 = (int) Math.ceil(yLabelD);
            if (yL2 < 1) {
                yL2 = 1;
            }
            int yL1 = yL2 - 1;
            double gL1 = grayValue(WIN_HEIGHT - YMARGIN - 1 - yL1);
            double gL2 = grayValue(WIN_HEIGHT - YMARGIN - 1 - yL2);
            double grayLabel = gL1 * (yL2 - yLabelD) + gL2 * (yLabelD - yL1);
            if (active) {
                ip.drawString(d2s(grayLabel), x + 5, yLabel + fontHeight / 2);
            }

            int iLength = metrics.stringWidth(d2s(grayLabel));
            if (iLength > maxLength) {
                maxLength = iLength;
            }
        }
        return maxLength;
    }

    String d2s(double d) {
        if ((int) d == d) {
            return IJ.d2s(d, 0);
        } else {
            return IJ.d2s(d, decimalPlaces);
        }
    }

    int getFontHeight() {
        Image img = GUI.createBlankImage(64, 64); //dummy version to get fontHeight
        Graphics g = img.getGraphics();
        int fontType = boldText ? Font.BOLD : Font.PLAIN;
        Font font = new Font("SansSerif", fontType, fontSize);
        FontMetrics metrics = g.getFontMetrics(font);
        return metrics.getHeight();
    }

    /* This method and the next are needed because ip.getPixelsCopy() does not work as expected.*/
    protected void storePixels(ImageProcessor ip) {
        if (ip instanceof ByteProcessor) {
            byte[] pixels = (byte[]) ip.getPixels();
            int n = pixels.length;
            byteStorage = new byte[n];
            for (int i = 0; i < n; i++) {
                byteStorage[i] = pixels[i];
            }
        } else if (ip instanceof ColorProcessor) {
            int[] pixels = (int[]) ip.getPixels();
            int n = pixels.length;
            intStorage = new int[n];
            for (int i = 0; i < n; i++) {
                intStorage[i] = pixels[i];
            }
        } else if (ip instanceof ShortProcessor) {
            short[] pixels = (short[]) ip.getPixels();
            int n = pixels.length;
            shortStorage = new short[n];
            for (int i = 0; i < n; i++) {
                shortStorage[i] = pixels[i];
            }
        } else if (ip instanceof FloatProcessor) {
            float[] pixels = (float[]) ip.getPixels();
            int n = pixels.length;
            floatStorage = new float[n];
            for (int i = 0; i < n; i++) {
                floatStorage[i] = pixels[i];
            }
        }
    }

    protected void restorePixels(ImageProcessor ip) {
        if (ip instanceof ByteProcessor) {
            if (byteStorage != null) {
                if (byteStorage.length == (ip.getWidth() * ip.getHeight())) {
                    byte[] pixels = (byte[]) ip.getPixels();
                    int n = pixels.length;
                    for (int i = 0; i < n; i++) {
                        pixels[i] = byteStorage[i];
                    }
                }
            }
        } else if (ip instanceof ColorProcessor) {
            if (intStorage != null) {
                if (intStorage.length == (ip.getWidth() * ip.getHeight())) {
                    int[] pixels = (int[]) ip.getPixels();
                    int n = pixels.length;
                    for (int i = 0; i < n; i++) {
                        pixels[i] = intStorage[i];
                    }
                }
            }
        } else if (ip instanceof ShortProcessor) {
            if (shortStorage != null) {
                if (shortStorage.length == (ip.getWidth() * ip.getHeight())) {
                    short[] pixels = (short[]) ip.getPixels();
                    int n = pixels.length;
                    for (int i = 0; i < n; i++) {
                        pixels[i] = shortStorage[i];
                    }
                }
            }
        } else if (ip instanceof FloatProcessor) {
            if (floatStorage != null) {
                if (floatStorage.length == (ip.getWidth() * ip.getHeight())) {
                    float[] pixels = (float[]) ip.getPixels();
                    int n = pixels.length;
                    for (int i = 0; i < n; i++) {
                        pixels[i] = floatStorage[i];
                    }
                }
            }
        }

    }

    Color getColor(String color) {
        Color c = Color.white;
        if (color.equals(colors[1])) {
            c = Color.black;
        } else if (color.equals(colors[2])) {
            c = Color.red;
        } else if (color.equals(colors[3])) {
            c = Color.green;
        } else if (color.equals(colors[4])) {
            c = Color.blue;
        } else if (color.equals(colors[5])) {
            c = Color.yellow;
        } else if (color.equals(colors[6])) {
            c = null;
        }
        return c;
    }

    public void actionPerformed(ActionEvent e) {
        if (impData == null) {
            IJ.noImage();
            return;
        }

        Object b = e.getSource();
        if (b == redraw) {
            clearLegend(this.imp);
            showLegend(impData, this.imp, 0, 0);
        } else if (b == insert) {
            storePixels(impData.getProcessor());
            Roi roi = impData.getRoi();
            if (roi != null) {
                Rectangle r = roi.getBoundingRect();
                showLegend(impData, impData, r.x, r.y);
            } else if (nw.getState()) {
                showLegend(impData, impData, INSERT_PAD, INSERT_PAD);
            } else if (ne.getState()) {
                showLegend(impData, impData, impData.getWidth() - win_width - INSERT_PAD, INSERT_PAD);
            } else if (sw.getState()) {
                showLegend(impData, impData, INSERT_PAD, impData.getHeight() - WIN_HEIGHT - INSERT_PAD);
            } else if (se.getState()) {
                showLegend(impData, impData, impData.getWidth() - win_width - INSERT_PAD,
                        impData.getHeight() - WIN_HEIGHT - INSERT_PAD);
            }
        } else if (b == unInsert) {
            restorePixels(impData.getProcessor());
            impData.updateAndDraw();
        } else if (b == setup) {
            GenericDialog gd = new GenericDialog("Legend Setup");
            gd.addChoice("Fill color: ", colors, fillColor);
            gd.addChoice("Box outline color: ", colors, boxOutlineColor);
            gd.addChoice("Bar outline color: ", colors, barOutlineColor);
            gd.addNumericField("Box Width ", win_width, 0);
            gd.addNumericField("Number of labels", numLabels, 0);
            gd.addNumericField("Decimal places", decimalPlaces, 0);
            gd.addNumericField("Font Size", fontSize, 0);
            gd.addChoice("Label color: ", colors, textColor);
            gd.addCheckbox("Bold Text", boldText);
            gd.addCheckbox("Automatic Box Width", autoWidth);
            gd.showDialog();
            if (gd.wasCanceled()) {
                return;
            }
            fillColor = gd.getNextChoice();
            boxOutlineColor = gd.getNextChoice();
            barOutlineColor = gd.getNextChoice();
            int win_widthInput = (int) gd.getNextNumber();
            numLabels = (int) gd.getNextNumber();
            decimalPlaces = (int) gd.getNextNumber();
            fontSize = (int) gd.getNextNumber();
            textColor = gd.getNextChoice();
            boldText = gd.getNextBoolean();
            autoWidth = gd.getNextBoolean();
            if (!autoWidth) {
                win_width = win_widthInput;
            }
            clearLegend(this.imp);
            showLegend(impData, this.imp, 0, 0);
        }
    }
}


//}
