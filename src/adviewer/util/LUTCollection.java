package adviewer.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * LUTCollection.java 
 * This is a collection of imagej .lut files. This class will take in the 
 * directory where they are stored (defined in adviewer.config) and then 
 * build a hash map of luts
 * @author Brian Freeman (bfreeman@jlab.org)
 */
public class LUTCollection {

    public String LUTPATH = "./lut/";

    private ArrayList<String> results;
    private File[] files;
    public HashMap<String, String> map;

    public File configFile;
    public Scanner sc;

    /**
     * Constructor takes in the directory where the.lut files are stored
     * @param lutpath 
     * @param debug 
     */
    public LUTCollection(String lutpath, boolean debug) {
        results = new ArrayList<String>();
        map = new HashMap<String, String>();

        try {

            files = new File(lutpath).listFiles();

            getFileNames();
            //printList();
           
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getLUTPath() {
        if (sc == null) {
            return;
        }

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            //System.out.println(line);
            String[] temp = line.split("=");
            if (temp[0].equals("LUTPATH")) {
                LUTPATH = temp[1];
                //System.out.println("Config found..... ["+ LUTPATH +"]");

            }
        }
    }

    public String getLUTPATH() {
        return LUTPATH;
    }

    public ArrayList<String> getResults() {
        return results;
    }

    public File[] getFiles() {
        return files;
    }

    public HashMap<String, String> getMap() {
        return map;
    }

    public void getFileNames() {
        // TODO Auto-generated method stub
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isFile() && file.getName().contains(".lut")) {

                results.add(file.getAbsolutePath());

                String[] temp = file.getName().split(".lut");

                if (temp.length > 0) {
                    map.put(temp[0], file.getAbsolutePath());
                }
            }
        }

    }

    public SortedSet<String> getSortedKeys(){
        
        return new TreeSet<String>(map.keySet());
        
    }
    
    public void printList() {
        for (String s : map.keySet()) {
            System.out.println(s);
        }
    }


}
