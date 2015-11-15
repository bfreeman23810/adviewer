/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.plot;

import adviewer.util.Helpers;
import adviewer.util.Log;
import ij.gui.Plot;
import static ij.gui.Plot.BOTTOM_MARGIN;
import static ij.gui.Plot.LEFT_MARGIN;
//import static ij.gui.Plot.MIN_FRAMEHEIGHT;
//import static ij.gui.Plot.MIN_FRAMEWIDTH;
import static ij.gui.Plot.RIGHT_MARGIN;
import static ij.gui.Plot.TOP_MARGIN;
import ij.measure.CurveFitter;
import ij.plugin.frame.Fitter;
import ij.util.Tools;
import java.awt.Dimension;

/**
 *
 * @author bfreeman
 */
public class PlotPlus extends Plot {

    /**
     * minimum width of frame area in plot
     */
    public int MIN_FRAMEWIDTH = 260;
    /**
     * minimum width of frame area in plot
     */
    public int MIN_FRAMEHEIGHT = 190;
    //The following the margin sizes actually used. They are modified for font size and also scaled for high-resolution plots
    public int leftMargin = 20, rightMargin = 20, topMargin = 13, bottomMargin = 37;
    private final boolean debug;
    public boolean isVertical = false;
    public float[] xValues;
    public float[] yValues;
    public double[] xFit;
    public double[] yFit;

    public double max = 0.0;
    public double min = 0.0;
    public double centroid = 0.0;
    public double dev = 0.0;
    public double fwhm = 0.0;

    public CurveFitter fitter;

    public PlotPlus(String title, String xLabel, String yLabel, double[] xValues, double[] yValues, boolean debug) {
        super(title, xLabel, yLabel, xValues, yValues);
        this.debug = debug;
        this.xValues = Helpers.toFloat(xValues);
        this.yValues = Helpers.toFloat(yValues);
    }

    public PlotPlus(String title, String xLabel, String yLabel, float[] xValues, float[] yValues, boolean debug) {
        super(title, xLabel, yLabel, xValues, yValues);
        this.debug = debug;
        this.xValues = xValues;
        this.yValues = yValues;
    }

    /**
     * The minimum plot size including borders, in pixels (at scale=1)
     */
    public Dimension getMinimumSize() {

        return new Dimension(MIN_FRAMEWIDTH + leftMargin + rightMargin,
                MIN_FRAMEHEIGHT + topMargin + bottomMargin);

    }

    public void setFitParams(double min, double max, double cent, double rms, double fwhm) {
        this.max = max;
        this.min = min;
        this.centroid = cent;
        this.dev = rms;
        this.fwhm = fwhm;

    }

    public void fitGaussian() {

        fitter = new CurveFitter(Helpers.toDouble(xValues), Helpers.toDouble(yValues));
        fitter.doFit(CurveFitter.GAUSSIAN);

        //double y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d));
        double[] params = fitter.getParams();
        if (params.length > 3) {

            min = params[0];
            max = params[1];
            centroid = params[2];
            dev = params[3];

            setYGaussFit(min, max, centroid, dev, fitter.getXPoints());

            if (isVertical) {

                this.addPoints(fitter.getYPoints(), yFit, this.LINE);
                //Log.log("fit y" , debug);
            } else {
                this.addPoints(fitter.getXPoints(), yFit, this.LINE);

            }
        }

        //Log.log(fitter.getResultString() + "\n\n", debug);
    }

    public double getGaussPoint(double a, double b, double c, double d, double x) {

        return a + (b - a) * Math.exp(-(x - c) * (x - c) / (2 * d * d));

    }

    public void setYGaussFit(double a, double b, double c, double d, double[] xVals) {
        yFit = new double[yValues.length];

        for (int i = 0; i < xVals.length; i++) {

            yFit[i] = getGaussPoint(a, b, c, d, xVals[i]);

        }
    }

}
