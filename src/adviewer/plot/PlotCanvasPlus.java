/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.plot;

import adviewer.util.Log;
import ij.ImagePlus;
import ij.gui.ImageWindow;
import ij.gui.Plot;
import ij.gui.PlotCanvas;
import ij.gui.PlotWindow;
import java.awt.Dimension;

/**
 *
 * @author root
 */
public class PlotCanvasPlus extends PlotCanvas {

    public Plot plot;
    private final boolean debug;
    
    public PlotCanvasPlus(ImagePlus imp , boolean debug) {
        
       super(imp);
       this.debug = debug;
       
    }

    public void setPlot(Plot plot) {
            super.setPlot( plot ); 
            this.plot = plot;
    }
    
    public void resizeCanvas(int width, int height) {
        if (plot == null || plot.isFrozen()) {
            resizeCanvas(width, height);
            return;
        }
        resetMagnification();
        //IJ.log("resizeCanvas "+width+"x"+height);
        //if (width == oldWidth && height == oldHeight) {
          //  return;
        //}
        if (plot == null) {
            return;
        }
        //ImageWindow win = imp.getWindow();
        //if (win == null || !(win instanceof PlotWindow)) {
          //  return;
        //}
        //if (!((PlotWindow) win).layoutDone) {
          //  return;				// window layout not finished yet?
       // }
       // ((PlotWindow) win).updateMinimumSize();
        Dimension minSize = plot.getMinimumSize();
        Log.log("size" + minSize.toString(), debug);
        int plotWidth = width < minSize.width ? minSize.width : width;
        int plotHeight = height < minSize.height ? minSize.height : height;
       
        plot.setSize(plotWidth, plotHeight);
        setSize(width, height);
        //oldWidth = width;
        //oldHeight = height;
    }

    public void resetMagnification() {
		magnification = 1.0;
		srcRect.x = 0;
		srcRect.y = 0;
	}
    
}
