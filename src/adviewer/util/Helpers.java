/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.util;

import adviewer.gui.PlotPlus;
import adviewer.gui.PlotProfilePlus;
import adviewer.image.ImagePlusPlus;
import ij.IJ;
import ij.ImagePlus;
import ij.Prefs;
import ij.gui.Line;
import ij.gui.Plot;
import ij.gui.ProfilePlot;
import ij.gui.Roi;
import ij.gui.Toolbar;
import ij.io.SaveDialog;
import ij.measure.Calibration;
import ij.util.Tools;
import java.awt.Rectangle;
import java.io.File;
import java.io.PrintWriter;

/**
 *
 * @author root
 */
public class Helpers extends Tools {

    public static ProfilePlot pp;
    
    /**
     * getPath was adapted from a private function in ij.io.FileSaver
     *
     * @param type
     * @param extension
     * @return
     */
    public static String getPath(String type, String extension, String name) {


        SaveDialog sd = new SaveDialog("Save as " + type, name, extension);
        name = sd.getFileName();
        if (name == null) {
            return null;
        }
        String directory = sd.getDirectory();

        String path = directory + name;
        return path;
    }

    public static void saveAsPgm(ImagePlus impp, String path, boolean debug) {
        if(path==null) path = Helpers.getPath("Text", ".pgm","new");
        
        File file = new File(path);
        try {

            //int[] pixVals = impp.getRoiAsIntArray();
            int[][] pixVals = impp.getProcessor().getIntArray();
            pixVals = Helpers.transposeInt(pixVals); //transpose the matrix
            
            PrintWriter writer = new PrintWriter(file);
            if (!file.canWrite()) {
                Log.log("Somtehing went wrong .... " , debug);
                return;
            } else {
               
                Log.log("Pixel array length = " + pixVals.length, debug);
                writer.println("P2");
                writer.println((int) impp.getWidth() + " " + (int) impp.getHeight());
                writer.println(255);
                for (int i = 0 ; i < pixVals.length ; i++) {

                    for(int j = 0 ; j < pixVals[i].length ;j++){
                        writer.print(pixVals[i][j] + " ");
                    }
                   
                    writer.println();

                }

            }

            writer.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void saveRoiAsPgm(ImagePlusPlus impp, String path, boolean debug) {
        impp.copy(); //copy the current selection or all
        Helpers.saveAsPgm( ImagePlus.getClipboard(), path, debug);
    }
    
     public static void saveAsDat(ImagePlus impp, String path, boolean isX,boolean debug) {
        if(path==null) path = Helpers.getPath("Data", ".dat","new");
        
        File file = new File(path);
        try {

            //int[] pixVals = impp.getRoiAsIntArray();
            //int[][] pixVals = impp.getProcessor().getIntArray();
            //if(isX == false) pixVals = Helpers.transposeInt(pixVals); //transpose the matrix
            double[] series = null;
            if(isX) series = getIntXAverageSeries(impp, debug);
            else series = getIntYAverageSeries(impp, debug);
            
            PrintWriter writer = new PrintWriter(file);
            if (!file.canWrite()) {
                Log.log("Somtehing went wrong .... " , debug);
                return;
            } else {
               
                Log.log("series length = " + series.length, debug);
                //writer.println("P2");
                //writer.println((int) impp.getWidth() + " " + (int) impp.getHeight());
                //writer.println(255);
//                for (int i = 0 ; i < pixVals.length ; i++) {
//                    
//                    int sum = 0;
//                    
//                    for(int j = 0 ; j < pixVals[i].length ;j++){
//                        sum = sum+pixVals[i][j];
//                    }
//                   
//                    writer.println( ( sum/pixVals[i].length ) );
//
//                }
                for (int i = 0 ; i < series.length ; i++) {
                    
  
                    writer.println( series[i] );

                }

            }

            writer.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    
      public static void saveRoiAsDat(ImagePlusPlus impp, String path,boolean isX, boolean debug) {
        impp.copy(); //copy the current selection or all
        
        Helpers.saveAsDat( ImagePlus.getClipboard(), path, isX , debug);
    }
    
     
    /**
     * Transposes the 2D int matrix , returns the new transposed array
     * @param intArr
     * @return 
     */
    public static int[][] transposeInt(int[][] intArr){
        int columns = intArr[0].length;
        int rows = intArr.length;
        int[][] returnArr = new int[columns][rows];
        for(int i = 0 ; i < rows ; i++){
            for(int j = 0 ; j < columns; j++){
                returnArr[j][i] = intArr[i][j]; 
            }
        }
        return returnArr;
    }
    
    public static float[] reverseFloatArray(float[] input){
        
        if(input == null){
            return null;
        }
        
        float[] output = new float[input.length];
        
        int j = 0;
        for(int i = input.length-1 ; i > 0 ; i--){
            output[j] = input[i];
            j++;
        }
            
        return output;
        
    }
    
    public static double[] getIntXAverageSeries(ImagePlus imp , boolean debug){
        
           int tool = Toolbar.getToolId();
        if(tool == Toolbar.LINE || tool == Toolbar.POLYLINE || tool == Toolbar.FREELINE){
            pp = new ProfilePlot(imp);
            Log.log("Selection is " + Toolbar.getToolName(), debug);
             if(pp.getProfile() !=null) return pp.getProfile();
         }
        if(tool == Toolbar.RECTANGLE){
            pp = new ProfilePlot(imp);
            Log.log("Selection is " + Toolbar.getToolName() , debug);
             if(pp.getProfile() !=null) return pp.getProfile();
         }
        
        double[] returnArr = new double[imp.getWidth()];
        
         int[][] pixVals = imp.getProcessor().getIntArray();
         
         for (int i = 0 ; i < pixVals.length ; i++) {
                    
                    int sum = 0;
                    
                    for(int j = 0 ; j < pixVals[i].length ;j++){
                        sum = sum+pixVals[i][j];
                    }
                   
                    returnArr[i] = (double) (sum/pixVals[i].length);

                }
         
        return returnArr;
    }
    
    public static double[] getIntYAverageSeries(ImagePlus imp , boolean debug){
       
       int tool = Toolbar.getToolId();
        if(tool == Toolbar.LINE || tool == Toolbar.POLYLINE || tool == Toolbar.FREELINE){
            pp = new ProfilePlot(imp);
            Log.log("Selection is " + Toolbar.getToolName(), debug);
             if(pp.getProfile() !=null) return pp.getProfile();
         }
        if(tool == Toolbar.RECTANGLE){
            pp = new ProfilePlot(imp , true);
            Log.log("Selection is " + Toolbar.getToolName() , debug);
             if(pp.getProfile() !=null) return pp.getProfile();
         }
        
        double[] returnArr = new double[imp.getHeight()];
        
         int[][] pixVals = imp.getProcessor().getIntArray();
         pixVals = Helpers.transposeInt(pixVals);
         
         for (int i = 0 ; i < pixVals.length ; i++) {
                    
                    int sum = 0;
                    
                    for(int j = 0 ; j < pixVals[i].length ;j++){
                        sum = sum+pixVals[i][j];
                    }
                   
                    returnArr[i] = (double) (sum/pixVals[i].length);

                }
        return returnArr;
    }
    
    public static Plot getPlot(ImagePlus imp, boolean isX, boolean debug) {
		Roi roi = imp.getRoi();
		//if (roi==null && !firstTime)
		//	IJ.run(imp, "Restore Selection", "");
		if (roi==null || !(roi.isLine()||roi.getType()==Roi.RECTANGLE)) {
                    Log.log("getPlot is setting roi to full canvas ... " , debug);
                    imp.setRoi( new Roi( new Rectangle( imp.getWidth() , imp.getHeight() ) ) );
                   //return null;
			
		}
		
		ProfilePlot pp = new ProfilePlot(imp, isX);
		return pp.getPlot();
	}
    
    public static PlotPlus getPlotPlus(ImagePlus imp, boolean isX, boolean showFit, boolean debug) {
		Roi roi = imp.getRoi();
		//if (roi==null && !firstTime)
		//	IJ.run(imp, "Restore Selection", "");
		if (roi==null || !(roi.isLine()||roi.getType()==Roi.RECTANGLE)) {
                    Log.log("getPlot is setting roi to full canvas ... " , debug);
                    imp.setRoi( new Roi( new Rectangle( imp.getWidth() , imp.getHeight() ) ) );
                   //return null;
			
		}
		
		PlotProfilePlus pp = new PlotProfilePlus(imp, isX,showFit, debug);
		return pp.getPlot();
	}
    
    public static Plot getPlot2(ImagePlus imp, boolean isX, boolean debug) {
		Roi roi = imp.getRoi();
		if (roi==null || !(roi.isLine()||roi.getType()==Roi.RECTANGLE)) {
			
			return null;
		}
                
		ProfilePlot pp = new ProfilePlot(imp, isX);
		return pp.getPlot();
	}
    
    public static PlotPlus getPlotPlus2(ImagePlus imp, boolean isX, boolean showFit, boolean debug) {
		Roi roi = imp.getRoi();
		if (roi==null || !(roi.isLine()||roi.getType()==Roi.RECTANGLE)) {
			
			return null;
		}
                
		PlotProfilePlus pp = new PlotProfilePlus(imp, isX, showFit,  debug);
		return pp.getPlot();
	}
}
