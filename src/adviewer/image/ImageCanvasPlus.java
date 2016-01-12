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
    
    public ImageCanvasPlus(ImagePlusPlus imp){
        super(imp);
        Log.log("ImageCanvasPlusCreated");
    }
    
    
    @Override
    public void paint(Graphics g){
        
        int width = (int) (imp.getWidth()*magnification);
        int height = (int) (imp.getHeight()*magnification);
        
       super.setSize( width , height );
       
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
