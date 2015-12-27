/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

/**
 *
 * @author root
 */
public class ViewerCollection {
    
    
    public HashMap<String, Viewer> viewers;
    public ArrayList<Viewer> viewerList;
    public boolean debug;
   
    public long time;
    public long prevTime;
    
    int counter = 0;
    
    //public String scriptToGetList = "/root/workspace/java/adviewer/listViewers" ;
    //public String scriptToViewerParams = "/root/workspace/java/adviewer/fakeViewerSigma" ;
    
    public String scriptToGetList = "/cs/dvlhome/apps/a/adViewer/dvl_2-0/src/adviewer/bin/getViewerList" ;
    public String scriptToViewerParams = "/cs/dvlhome/apps/a/adViewer/dvl_2-0/src/adviewer/bin/getViewerSigmas " ;
    
    public ViewerCollection(boolean debug){
        init(debug);
    
    }
    
    public void init(boolean debug){
        viewers = new HashMap<String, Viewer>();
        viewerList=new ArrayList<Viewer>();
        this.debug = debug;
        
        
        time = new Date().getTime();
        prevTime = time;
        
        //diff should be equal to TIMERWAIT time, since this is what triggers an update
        //double diff = time - prevTime;
    }
    
    public void setLists(){
    
        try{ 
            Log.log( "Running ... " + scriptToGetList , debug);
            String s = SystemCommand.exec(scriptToGetList);
            Log.log(s,debug);
            
            Scanner scanner = new Scanner(s);
            while(scanner.hasNextLine()){
                Viewer v = new Viewer();
                v.debug = this.debug;
                v.name = scanner.nextLine();
               
                v.insertedPV =v.name+"T";
                
                
                
                String sigmas = SystemCommand.exec(scriptToViewerParams + " -n " + v.name);
                String[] sig = sigmas.split(",");
                
                v.sigmaX = sig[0];
                v.sigmaY = sig[1];
                v.setAspectRatio(v.sigmaX, v.sigmaY);
                
                Log.log(v.toString() , debug);
            
                viewers.put( v.name , v);
               
                viewerList.add(v);
                
            }
        }
        catch(Exception e){
            Log.log("Error loading the viewer list", debug);
            if(debug ) e.printStackTrace() ;
        }
        
        
    }
    
    public String getInserted(){
        
        String s= "";
        prevTime = time;
        
        
        
        /*if(debug){
            if(counter == 2) counter = 0;
            else counter++;
            if(counter == 0){
                return viewerList.get(0).name + " " +viewerList.get(1).name ;
            }
            else{
                return viewerList.get(2).name ;
            }
            
        }*/
        
        if(viewers != null && viewers.size()>0 ){
            
            
            for(Viewer v : viewerList){
                String x = SystemCommand.caget(v.insertedPV.trim());
                
                //Log.log(v.insertedPV.trim() + " = " + x , debug);
                
                if(x.trim().equals("1")){
                     Log.log(v.toString(), debug);
                     s+=v.name+" ";
                }else {
                    
                   //Log.log("None", debug); 
                   continue;
                }
                
               
            }
            
        }
        else{
            //Log.log("viewer list is empty .... or null",debug);
        }
        
        time = new Date().getTime();
        double diff = time  - prevTime;
        //Log.log("Time = " + diff , debug);
        
        return s;
        
        
    }
    
    
   /** public static void main(String[] args) throws InterruptedException{
        ViewerCollection vc = new ViewerCollection(true);
        vc.setLists();
       
        while(true){
            String s = vc.getInserted();
            if(!s.equals("")){
                Log.log( vc.viewers.get(s), vc.debug );
            }
            Thread.sleep(500);
        }
    } **/
    
    
}
