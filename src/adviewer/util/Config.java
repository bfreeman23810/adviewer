/**
 * Config.java
 * Get the contents of adviewer.cong
 * 
 */
package adviewer.util;

import adviewer.util.Log;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

/**
 *
 * @author root
 */
public class Config {
    
    //to use a different config name change config here
    public String config = "adviewer.config";
    
    private String lutPath;      //path where look up table (.lut) are stored 
    private String iconPath;     //path to icons 
    private String classPath;    //path to class files
    private String camConfig;    //config file where camera information is stored
    private String viewerList;      //text file of viewers
    private String getViewerList;   //script to get list of viewers (creates viewers.txt)
    private String top;             //top level dir
    private String logPath;             //dir to write log files

    
    
    //these are the strings we will seearch for in the config file
    public static final String TOP = "TOP";     
    public static final String LOG = "LOG";     
    public static final String LUTPATH = "LUTPATH";     
    public static final String CLASSPATH = "CLASSPATH";
    public static final String CAMCONFIG = "CAMCONFIG";
    public static final String ICONPATH = "ICONPATH";
    public static final String VIEWERTXT = "VIEWERTXT";
    public static final String GETVIEWERLIST = "GETVIEWERLIST";
    
    public boolean debug;
    
    public Config(String currDir, boolean debug){
        this.debug = debug;
        getContents(currDir);
    }
    
    /**
     * This method will parse through the config file searching for the listed parameters
     */
    public void getContents(String currDir){
        try{
            //get the path where this file launches from
            //Log.log("Curr Directory = " +currDir , debug);
           // Path currentRelativePath = Paths.get("");
            //String execPath = currentRelativePath.toAbsolutePath().toString();
            String config = currDir + "/config/" + this.config; //change to absolute path version of config
            this.config = config;
            
            //Log.log("Config path = : " + config , this.debug);
            
            File f = new File(config);
            Scanner scanner = new Scanner(f);
            
            while(scanner.hasNextLine()){
                String s = scanner.nextLine();
                String[] temp = s.split("=");
                
                if(temp[0].equals(TOP)) this.top=temp[1]; 
                if(temp[0].equals(LUTPATH)) this.lutPath=temp[1]; 
                if(temp[0].equals(LOG)) this.logPath=temp[1]; 
                if(temp[0].equals(ICONPATH)) this.iconPath=temp[1]; 
                if(temp[0].equals(CLASSPATH)) this.classPath=temp[1]; 
                if(temp[0].equals(CAMCONFIG)) this.camConfig=temp[1]; 
                if(temp[0].equals(VIEWERTXT)) this.viewerList=temp[1]; 
                if(temp[0].equals(GETVIEWERLIST)) this.getViewerList=temp[1]; 
            }
            
            //Log.log( toString() , debug);
            
        }
        catch(Exception e){
            Log.log("Error in processing the config file....", debug);
            if(debug) e.printStackTrace();
        }
    }
    
    /**
     * Print the config params out, to be sure we are using the correct paths
     * @return 
     */
    public String toString() {
        return "[adviewer.config = "+ this.config +" ,\n"
                + " lutPath = "+ this.lutPath +",\n"
                + " log = "+ this.logPath +",\n"
                + " classPath =  "+this.classPath+",\n"
                + "camConfig = "+ this.camConfig +",  \n"
                + "iconPath = "+ this.iconPath +" , \n"
                + "viewerList = "+ this.viewerList +" , \n"
                + "getViewerList = "+ this.getViewerList +"  ]\n";
    
    }
    
    //getters
     public String getConfig() {
        return config;
    }

    public String getLutPath() {
        return lutPath;
    }

    public String getIconPath() {
        return iconPath;
    }

    public String getClassPath() {
        return classPath;
    }

    public String getCamConfig() {
        return camConfig;
    }
    
    public String getViewerList() {
        return viewerList;
    }

    public String getGetViewerList() {
        return getViewerList;
    }
   
    
    /*public static void main(String[] args){
        
        Config config = new Config(true);
        
        
    }*/

    public String getLogPath() {
        return logPath;
    }


    public String getTop() {
        return top;
    }
    
}
