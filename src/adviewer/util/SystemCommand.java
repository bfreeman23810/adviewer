package adviewer.util;

import java.io.IOException;
import java.util.Scanner;

public class SystemCommand {

    //get the current working directory from System
    public static String pwd() {
        String result = "";

        try {

            // using the Runtime exec method:
            Process p = Runtime.getRuntime().exec("pwd");

            Scanner in = new Scanner(p.getInputStream());

            while (in.hasNext()) {

                result += in.nextLine();

            }

        } catch (Exception e) {
            System.out.println("An error occured while getting the pwd (SystemCommand.pwd())");
        }

        return result;
    }

    //get the current working directory from System
    public static String cd(String path) {
        String result = "";

        try {

            // using the Runtime exec method:
            Process p = Runtime.getRuntime().exec("cd " + path);

            Scanner in = new Scanner(p.getInputStream());

            while (in.hasNext()) {

                result += in.nextLine();

            }

        } catch (Exception e) {
            System.out.println("An error occured while getting the pwd (SystemCommand.cd())");
        }

        return result;
    }

    public static String caget(String aPV) {

        String result = "";

        try {

            // using the Runtime exec method:
            Process p = Runtime.getRuntime().exec("caget -t -n " + aPV);

            Scanner in = new Scanner(p.getInputStream());

            while (in.hasNext()) {

                result += in.nextLine() + "\n";

            }

        } catch (IOException e) {

            //e.printStackTrace();
            // System.out.println("Error in getting IC values .... returning zero. Failed at caGet("+aPV+")");
            //System.exit(-1);
            //setting the values to some number for testing, need to back to zero after testing
            result += "null\n";

        }

        return result;

    }

    public static String exec(String command) {

        String result = "";

        try {

            // using the Runtime exec method:
            Process p = Runtime.getRuntime().exec(command);

            Scanner in = new Scanner(p.getInputStream());

            while (in.hasNext()) {

                result += in.nextLine() + "\n";

            }

        } catch (IOException e) {

            e.printStackTrace();
            // System.out.println("Error in getting IC values .... returning zero. Failed at caGet("+aPV+")");
            //System.exit(-1);
            //setting the values to some number for testing, need to back to zero after testing
            result += "null\n";

        }

        return result;

    }

    /**
     * public static void main(String args[]){ SystemCommand sys = new
     * SystemCommand(); System.out.print( sys.exec("pwd") );
    }*
     */
}
