/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.image;

import adviewer.util.Log;
import ij.IJ;
import ij.Prefs;
import ij.gui.ImageCanvas;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

/**
 *
 * @author root
 */
public class ImageCanvasPlus extends ImageCanvas{
    
    private int savedWinWidth = 0;
    private int savedWinHeight = 0;
    
    public ImageCanvasPlus(ImagePlusPlus imp){
        super(imp);
        Log.log("ImageCanvasPlusCreated");
    }
    
    
    @Override
    public void paint(Graphics g){
        
        int width = (int) (imp.getWidth()*magnification);
        int height = (int) (imp.getHeight()*magnification);
        
        /**
         * 
         * this is to fix a bug that prevents the imageCanvas from auto resizing.
         * set win height and win width 
         */
        int winWidth = (int) (imp.getWindow().getWidth()*magnification);
        int winHeight = (int) (imp.getWindow().getHeight()*magnification); 
        
        /**
         * now check to see if the savedwin size is less...i.e if window size was changed
         */
        if( savedWinWidth < winWidth || savedWinHeight < winHeight){
            //if window size was change the reset the canvas to imp size multiplied by magnification
            super.setSize( width , height ); 
            Log.log("size set....");
            Log.log("Saved Width = " + savedWinWidth
            +"\nsaved Height = " + savedWinHeight);
        }
        
        
        savedWinWidth = winWidth;
        savedWinHeight = winHeight;
       
       //now call ImageCanvas.paint(Graphics g)
       super.paint(g);
        
    }
    
    
    public void setInterpolation(Graphics g, boolean interpolate) {
		if (magnification==1)
			return;
		else if (magnification<1.0 || interpolate) {
			Object value = RenderingHints.VALUE_RENDER_QUALITY;
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_RENDERING, value);
		} else if (magnification>1.0) {
			Object value = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, value);
		}
	}
    
    
    
}
