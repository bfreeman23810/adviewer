/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.plot;

import adviewer.util.Helpers;
import adviewer.util.Log;
import ij.ImagePlus;
import ij.gui.ProfilePlot;
import ij.measure.CurveFitter;
import java.awt.Color;

/**
 *
 * @author bfreeman
 */
public class PlotProfilePlus extends ProfilePlot {

    private final boolean debug;
    private boolean averageHorizontally;
    private CurveFitter fitter;
    public boolean showGaussFit = true;
    public double[] xFit;
    public double[] yFit;
    public PlotPlus plot;
    private boolean showFWHM = true;
    private double[] fwhmY;
    private double[] fwhmX;

    public PlotProfilePlus(ImagePlus imp, boolean averageHorizontally, boolean showFit, boolean debug) {
        super(imp, averageHorizontally);
        this.averageHorizontally = averageHorizontally;
        this.showGaussFit = showFit;
        this.debug = debug;
        this.imp = imp;
    }

    public PlotPlus getPlot(boolean isAveraged) {

        if (profile == null) {
            return null;
        }
        
        //if it is we do not want the averaged data, then change xValues and yValues
        if(isAveraged == false) {
            Log.log("Averaging off" , debug);
            if (averageHorizontally) {
                     xValues = Helpers.toFloat(  Helpers.getIntXSeries(imp, debug) );
                     
            }
            else{
                     xValues = Helpers.toFloat(  Helpers.getIntYSeries(imp, debug) );
            }
        }
        
        String xLabel = "Distance (" + units + ")";
        int n = profile.length;
        if (xValues == null) {
            xValues = new float[n];
            for (int i = 0; i < n; i++) {
                xValues[i] = (float) (i * xInc);
            }
        }

        float[] yValues = new float[n];
        for (int i = 0; i < n; i++) {
            yValues[i] = (float) profile[i];
        }
        //boolean fixedYScale = fixedMin!=0.0 || fixedMax!=0.0;

        this.yLabel = "";

        if (showGaussFit) {
            if (averageHorizontally) {
                fitGaussian(Helpers.toDouble(xValues), Helpers.toDouble(yValues));
            } else {
                fitGaussian(Helpers.toDouble(xValues), Helpers.toDouble(yValues));
            }
        }

        PlotPlus plot = null;
        //set plot obj
        if (averageHorizontally) {
            plot = new PlotPlus("Plot of " + getShortTitle(imp), yLabel, xLabel, Helpers.reverseFloatArray(yValues), xValues, debug);
        } else {
            plot = new PlotPlus("Plot of " + getShortTitle(imp), xLabel, yLabel, xValues, yValues, debug);
        }

        //set fit data
        if (showGaussFit && plot != null) {

            double xMin = xValues[0];
            double xMax = xValues[xValues.length - 1];

            double fwhm = (2 * Math.sqrt(2 * Math.log(2))) * dev;

            double fwhmX1 = cent - (fwhm / 2);
            double fwhmX2 = cent + (fwhm / 2);
            double fwhmY1 = getGaussPoint(max, min, cent, dev, fwhmX1);
            double fwhmY2 = getGaussPoint(max, min, cent, dev, fwhmX2);

            fwhmX = new double[2];

            fwhmY = new double[2];

            // Log.log(fwhmX1 + " : " + fwhmY1 + " : " + fwhmX2 + " : " + fwhmY2 , debug);
            if (!averageHorizontally) {
                fwhmX[0] = fwhmX1;
                fwhmX[1] = fwhmX2;

                fwhmY[0] = fwhmY1;
                fwhmY[1] = fwhmY2;

                plot.setFitParams(min, max, cent, dev, fwhm);

            } else {
                fwhmX[0] = fwhmY1;
                fwhmX[1] = fwhmY2;

                fwhmY[0] = xMax - fwhmX1;
                fwhmY[1] = xMax - fwhmX2;

                plot.setFitParams(min, max, (xMax - cent), dev, fwhm);

            }
        }
        //display the lines
        if (averageHorizontally) { //then is a vertically displayed plot and need to reverse the axises

            int y = plot.MIN_FRAMEHEIGHT;
            plot.isVertical = true;
            if (imp != null) {
                plot.MIN_FRAMEHEIGHT = imp.getHeight();
                plot.MIN_FRAMEWIDTH = y;
                // Log.log("min x = " + plot.MIN_FRAMEWIDTH + ", min y = " + plot.MIN_FRAMEHEIGHT , debug);
                //plot.setSize(plot.MIN_FRAMEWIDTH - (plot.leftMargin + plot.rightMargin), plot.MIN_FRAMEHEIGHT);
                plot.setSize(plot.MIN_FRAMEWIDTH, plot.MIN_FRAMEHEIGHT);

                //      //double[] a = Helpers.getMinMax(xValues);
                //plot.setLimits(a[0],a[1],fixedMin,fixedMax);
                if (showGaussFit && yFit != null) {
                    //Log.log("Y FIT", debug);
                    plot.setColor(Color.RED);
                    // plot.makeHighResolution("Test", (float) 2.0,true,true); //returns a high res image of the plot
                    plot.addPoints(Helpers.reverseFloatArray(Helpers.toFloat(yFit)), xValues, plot.LINE);
                    //plot.setScale((float) imp.getCanvas().getMagnification()); //sets the scale of the plot
                    plot.setColor(Color.BLUE);
//                        
                        if (this.showFWHM) {
                            plot.setColor(Color.BLUE, Color.RED);
                            plot.addPoints(fwhmX, fwhmY, PlotPlus.CONNECTED_CIRCLES);

                        }

                } else {
                    plot.setColor(Color.BLUE);
                }

            }
        } else {

            if (imp != null) {
               
                plot.MIN_FRAMEWIDTH = imp.getWidth()+35;
                plot.setSize(imp.getWidth(), plot.MIN_FRAMEHEIGHT);
               
                if (showGaussFit && yFit != null) {
                    plot.setColor(Color.RED);
                    plot.addPoints(xValues, Helpers.toFloat(yFit), plot.LINE);
                    plot.setColor(Color.BLUE);

                        if (this.showFWHM) {
                            plot.setColor(Color.BLUE, Color.RED);
                            plot.addPoints(fwhmX, fwhmY, PlotPlus.CONNECTED_CIRCLES);

                        }
                } else {
                    plot.setColor(Color.BLUE);
                }
            }

            //if( this.showFWHM ){
            //plot.setColor(Color.CYAN, Color.CYAN);
            //plot.addPoints(fwhmX, fwhmY, PlotPlus.CONNECTED_CIRCLES);
            //}
        }

//		if (fixedYScale) {
//			double[] a = Tools.getMinMax(xValues);
//			plot.setLimits(a[0],a[1],fixedMin,fixedMax);
//		}
        return plot;

    }

    String getShortTitle(ImagePlus imp) {
        String title = imp.getTitle();
        int index = title.lastIndexOf('.');
        if (index > 0 && (title.length() - index) <= 5) {
            title = title.substring(0, index);
        }
        return title;
    }
    double min = 0.0;
    double max = 0.0;
    double cent = 0.0;
    double dev = 0.0;

    public void fitGaussian(double[] xVals, double[] yVals) {

        fitter = new CurveFitter(xVals, yVals);
        if (xVals.length == 0 || yVals.length == 0) {
            return;
        }

        fitter.doFit(CurveFitter.GAUSSIAN);

        //double y = a + (b-a)*exp(-(x-c)*(x-c)/(2*d*d));
        double[] params = fitter.getParams();
        if (params.length > 3) {
            this.min = params[0];
            this.max = params[1];
            this.cent = params[2];
            this.dev = params[3];

            //Log.log("Parameter length = "+params.length, debug);
            setYGaussFit(min, max, cent, dev, fitter.getXPoints());

//            if (averageHorizontally) {
//                setYGaussFit(params[0], params[1], params[2], params[3], fitter.getXPoints());
//                //this.addPoints(fitter.getYPoints(), yFit, this.LINE);
//                //Log.log("fit y" , debug);
//            } else {
//                setYGaussFit(params[0], params[1], params[2], params[3], fitter.getXPoints());
//                //this.addPoints(fitter.getXPoints(), yFit, this.LINE);
//            }
        }

        // Log.log(fitter.getResultString() + "\n\n", debug);
    }

    /**
     *
     * @param a
     * @param b
     * @param c
     * @param d
     * @param x
     * @return
     */
    public double getGaussPoint(double a, double b, double c, double d, double x) {

        return (a + (b - a) * Math.exp(-(x - c) * (x - c) / (2 * d * d)));

    }

    public void setYGaussFit(double a, double b, double c, double d, double[] xVals) {
        yFit = new double[xVals.length];

        for (int i = 0; i < xVals.length; i++) {

            yFit[i] = getGaussPoint(a, b, c, d, xVals[i]);

        }
    }
}
