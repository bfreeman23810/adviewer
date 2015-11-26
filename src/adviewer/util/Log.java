/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.util;

import java.io.File;
import java.io.PrintWriter;
import javax.swing.JOptionPane;

/**
 *
 * @author root
 */
public class Log {

    public static PrintWriter writer;
    
    public static void d(String message, boolean debug , File logFile) {
        
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
            
            if(debug) System.out.println(s);
            
            if(logFile == null){ return;}
            else{
                try{
                    writer = new PrintWriter(logFile);
                
                    writer.println(s);
                }
                catch(Exception e){
                    return;
                }
            }
        
    }
    
    public static void log(String message, boolean debug) {
        if (debug) {
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
            
            System.out.println(s);
        }
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
            System.out.println(s);
        }
        
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
        
    }
}
