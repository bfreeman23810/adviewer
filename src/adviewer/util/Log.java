/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.util;

/**
 *
 * @author root
 */
public class Log {

    public static void log(String message, boolean debug) {
        if (debug) {
            String fullClassName = Thread.currentThread().getStackTrace()[2].getClassName();
            String className = fullClassName.substring(fullClassName.lastIndexOf(".") + 1);
            String methodName = Thread.currentThread().getStackTrace()[2].getMethodName();
            int lineNumber = Thread.currentThread().getStackTrace()[2].getLineNumber();

            System.out.println("// "+className + "." + methodName + "():" + lineNumber + ":\n//... " + message);
        }
    }

    public static String lineOut() {
        int level = 3;
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        return (" at " + traces[level] + " ");
    }
}
