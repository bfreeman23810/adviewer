/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.util;

import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

/**
 *
 * @author root
 */
public class JSONIO {
    
    public static ArrayList<JSONObject> jsonObjects;
    public static JSONArray jsonArray;
    public static boolean debug = true;
    
    
    /**
     * Take in file and return one string of json data
     * 
     * @return
     * @throws IOException
     */
    public static String readJSONFile(File inFile){
        String readFile = "";
        
        try{
            File fileIn = inFile;
            
            //if the file is not there then create the file
            if( fileIn.createNewFile() ){
                System.out.println(fileIn +" was created ");
            }
 
            FileReader fr = new FileReader(fileIn);
            Scanner sc = new Scanner(fr);
            
            
            while(sc.hasNext()){
                readFile += sc.nextLine().trim();
            }
  
            return readFile;
            
        }
        catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
       // System.out.println( readFile  );
        return readFile;
    }
    
    /**
     * not fully implemented .... 
     * @param inFile 
     */
    public static void setJSONArrayFromFile(File inFile){
        JSONIO.jsonArray = new JSONArray();
        try{
            String s = readJSONFile(inFile);
            
        }
        catch(Exception e){
            
            System.err.println("Problem with parsing JSON File .... JSONIO .... line 71");
            if(debug) e.printStackTrace();
            
        }
    }
 
     /**
     * setter for JSON Object ArrayList
     * @throws JSONException
     */
    public static void setJsonObjects(File inFile ) throws JSONException {
       
        JSONIO.jsonObjects = new ArrayList<JSONObject>() ;
        
        try {
            JSONIO.jsonObjects = parseReadFileString( readJSONFile( inFile ) );
        }
        catch ( Exception e ) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
     /**
     * 
     *   
     *
     * pre  :
     * post :
     *
     * @param s
     * @return
     * @throws JSONException
     */
    public static ArrayList<JSONObject> parseReadFileString(String s) throws JSONException{
        
        //System.out.println(s);
       
        if(s.contains( "[" )){
            s = s.substring( s.indexOf("[")+1 , s.indexOf("]") );
            
           if(s.contains("{")) {
                String one = s.substring( s.indexOf("{") , s.indexOf("}")+1 );
                s = s.replace( one+",", "" );
                
                
                
                while(!s.isEmpty()){
                    //System.out.println(one + "\n" + s);
                    jsonObjects.add( new JSONObject(one) );
                    one = s.substring( s.indexOf("{") , s.indexOf("}")+1 );
                    s = s.replace( one+",", "" );
                    
                    if(s.equals( one )){
                        jsonObjects.add( new JSONObject(s) );
                        break;
                    }
                }
           }
           else {
               System.err.println("parseReadFilestring() - This is an empty JSON array");
           }
           
            //defines the Info class variables from the JSON Objects
            //jsonObjects = defineClassVariables(jsonObjects );
            
            //System.out.println(jsonObjects.toString());
        }
        else {
            System.out.println("No string or it was an empty string , method : parseReadFileString()");
        }
        
           return jsonObjects;
        
         
    }
    
        /**   
     * 
     *
     * pre  :
     * post :
     *
     * @param info
     * @throws JSONException
     */
    public static String printJSONArray( String arrName ) throws JSONException{
        
        //convert the list back to a JSONObject list
       // this.jsonObjects =  infoListToJSONList( info );
        //this.setJsonStrings();
        String s;
        
        s= "var "+arrName+" = [";
        
        for( int i = 0 ; i < jsonObjects.size() ; i++ ) {
            
            if(i < jsonObjects.size()-1 ) {    
                s+= jsonObjects.get( i ).toString(5) + ",";
            }
            else {
                s+= jsonObjects.get( i ).toString(5);
            }
            
        }
        
        s+="];";
        
        
        return s;
    }
    
     /**
     *   
     *
     * pre  :
     * post :
     *
     * @param info
     * @param f
     * @throws JSONException
     */
 public static void printJSONArrayToFile( String arrName , File f) throws JSONException{
        
     //convert the list back to a JSONObject list
     //this.jsonObjects =  infoListToJSONList( info );
     //this.setJsonStrings();
     
         try {
             
             PrintWriter writer = new PrintWriter(f);
             
            writer.print(JSONIO.printJSONArray(arrName));
         }
         catch(Exception e) {
             e.printStackTrace( );
         }
        
    }
}
