/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.util;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 *
 * @author root
 */
public class Viewer {

    public String name;
    public String sigmaX;
    public String sigmaY;
    public String ratio;
    public String camera;
    public boolean debug;
    public String insertedPV;
    
    public Viewer(){
    
    }
    
    public Viewer(String name, String sigX, String sigY, String insertedPV, boolean debug) {
        this.name = name;
        this.sigmaX = sigX;
        this.sigmaY = sigY;
        this.debug = debug;
        this.insertedPV = insertedPV;
        
        setAspectRatio(sigX, sigY);
    }

    public String setAspectRatio(String num1, String num2) {
        String ratio = "";

        try {
            double d1 = Double.parseDouble(num1);
            double d2 = Double.parseDouble(num2);

            DecimalFormat df = new DecimalFormat("#.#");
            df.setRoundingMode(RoundingMode.CEILING);

            if (d1 > d2) {
                ratio = "["+df.format(d1 / d2) + "]:1";
            } else {
                 ratio = "1:["+df.format(d2 / d1) + "]";
            }

        } catch (Exception e) {
            Log.log("Problem with processing the aspect ratio", debug);
            e.printStackTrace();
        }

        this.ratio = ratio;
        return ratio;

    }

    public String toString(){
        String s = "";
        
        s += "Viewer = ["+name+","+ sigmaX +","+ sigmaY+ ","+ratio+"]";
        
        return s;
        
    }
    
    /**public static void main(String[] args){
        Viewer v = new Viewer("ITVXXXX" , "4.0245" , "2.3456" , true);
        System.out.println( v.toString()  );
        
    }**/
    
}
