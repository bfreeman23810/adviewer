/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

import adviewer.gui.ADWindow;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 *
 * @author root
 */
public class Log {

    public static Logger LOGGER = Logger.getLogger("MyLogger");
    
    public static File logFile;
    private static FileHandler fh;
    
    public Log(File logfile, boolean debug) throws FileNotFoundException, IOException{
        this.logFile=logfile;
        
        fh = new FileHandler(logFile.getAbsolutePath());  
       
        if(!debug){
            LOGGER.setUseParentHandlers(false);
        }
        
        LOGGER.addHandler(fh);
        SimpleFormatter formatter = new SimpleFormatter();  
        fh.setFormatter(formatter);   
    }
    
    public static void d(String message, boolean debug ) {
        
            String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

            String s = "// "+className + 
                    "." +
                    methodName + "():" + 
                    lineNumber +
                    ":\n//... " +
                    message;
            
            //if(debug) System.out.println(s);
            
           if(debug) LOGGER.info(s);
        
    }
    
    public static void log(String message, boolean debug) {
        
             String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

            String s = "// "+className + 
                    "." +
                    methodName + "():" + 
                    lineNumber +
                    ":\n//... " +
                    message;
        
        LOGGER.info(s);
        
    }
    
     public static void log(String message) {
        
             String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

            String s = "// "+className + 
                    "." +
                    methodName + "():" + 
                    lineNumber +
                    ":\n//... " +
                    message;
        
        LOGGER.info(s);
        
    }
    
    public static String lineOut() {
        int level = 3;
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        return (" at " + traces[level] + " ");
    }
    
    public static void err(String message, boolean debug) {
        if (debug) {
            String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

            String s = "// "+className + "." + methodName + "():" + lineNumber + ":\n//... " + message;
            LOGGER.severe(s);
        }
        
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        
    }
}
