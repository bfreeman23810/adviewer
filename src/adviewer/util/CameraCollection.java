package adviewer.util;

import adviewer.gui.ADWindow;
import ij.IJ;
import java.io.BufferedReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * generate a collections of cameras from a config file, supporting xml, txt, and json config files
 *
 * @author bfreeman
 *
 */
public class CameraCollection {

    public static String ADCONFIG = "/root/workspace/java/adviewer/config/adviewer.config";
    public String CAMERACONFIGPATH = "./config/cam.json";
    //String CAMERACONFIGPATH = "/home/brian/scripts/java/support/camList.txt";
    public boolean debug;
    File configFile;
    Scanner sc;
    public ArrayList<CameraConfig> cameras;
    public HashMap<String, CameraConfig> map;
    //from xml config
    public static final String CAMERAS = "camaeras";
    public static final String CAM = "cam";
    public static final String ID = "id";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String URL = "url";
    public static final String PV = "pv";
    public static final String IOC = "ioc";
    private Document dom;

    /**
     * Use default CONFIG File defined in this file
     */
    public CameraCollection() {

        cameras = new ArrayList<CameraConfig>();
        map = new HashMap<String, CameraConfig>();

        try {
            String path = getCameras(ADCONFIG);
            configFile = new File(path);
            sc = new Scanner(configFile);

            //getXMLList();
            parseJSON();           
        } catch (Exception e) {
            System.err.println("There was a problem with the config file .... please have a look ... adviewer.util.CameraCollection ..... line 57/n" + e.getMessage());
        }

    }

    /**
     * pass in a config file, this is the main config file. The actual cam list
     * xml file is defined there
     *
     * @param adconfigPath
     * @param debug
     */
    public CameraCollection(String adconfigPath, boolean debug) {

        cameras = new ArrayList<CameraConfig>();
        map = new HashMap<String, CameraConfig>();
        this.debug = debug;

        try {
            String path = getCameras(adconfigPath);
            configFile = new File(path);
            sc = new Scanner(configFile);

            //getList();
            //getXMLList();
            //parseXmlFile();
            //parseDocument();
            parseJSON();
           
            if (debug) {
                printCams();
            }
        } catch (Exception e) {
            System.err.println("There was a problem with the config file .... please have a look ... adviewer.util.CameraCollection ..... line 57\n");
            if (debug) {
                e.printStackTrace();
            }
        }

    }

    public final void parseJSON() {

        try {

            if (debug) {
                System.out.println(JSONIO.readJSONFile(configFile));
            }
            JSONIO.setJsonObjects(configFile); //sets the JSONObj Array from static class JSONIO
            setCamObjsFromJSONObjArray(JSONIO.jsonObjects); // now use the above array to map CameraConfig Objects
            if (debug) {
                System.out.println(JSONIO.printJSONArray("cameras"));
            }
        } catch (Exception e) {
        }

    }

    public String printCamListByID(){
        
        String s="";
        
        if(cameras!=null && !cameras.isEmpty()){
            for(CameraConfig cam : cameras){
                s+=cam.getId() + " : " + cam.getName() +"\n";
            }
        }
        
        return s;
        
    }
    
    /**
     * Maps the JSON Obj Array to CameraConfig Object
     *
     * @param jsonObjects
     */
    public void setCamObjsFromJSONObjArray(ArrayList<JSONObject> jsonObjects) {
        //try to iterate through array list and set the fields for cam objects

        if (jsonObjects == null && !jsonObjects.isEmpty()) {
            if (debug) {
                System.err.println("Something is wrong mapping JSON Objects to CameraConfig Obj ... line 133 in adviewer.util.CamereaCollection");
            }
            return;
        } else {
            for (JSONObject jsonObj : jsonObjects) {
                if (jsonObj == null) {
                    continue;
                }
                CameraConfig cam = new CameraConfig();

                if (jsonObj.has(NAME)) {
                    cam.setName((String) jsonObj.get(NAME));
                }
                if (jsonObj.has(ID)) {
                    cam.setId((String) jsonObj.get(ID));
                }
                if (jsonObj.has(URL)) {
                    cam.setUrl((String) jsonObj.get(URL));
                }
                 if (jsonObj.has(PV)) {
                    cam.setPvPrefix((String) jsonObj.get(PV));
                }
                if (jsonObj.has(TYPE)) {
                    cam.setConnectionType((String) jsonObj.get(TYPE));
                }
                if (jsonObj.has(IOC)) {
                    cam.setIocname((String) jsonObj.get(IOC));
                }

                cameras.add(cam);
                map.put(cam.getId(), cam);

            }
        }

    }

    public void printCams() {
        for (CameraConfig c : cameras) {
            System.out.println(c.toString());

        }
    }

    public final String getCameras(String adConfigPath) {
        try {
            sc = new Scanner(new File(adConfigPath));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] temp = line.split("=");
            //System.out.println(line);
            if (temp[0].equals("CAMCONFIG")) {
                CAMERACONFIGPATH = temp[1];
                if(this.debug) System.out.println("CAMERACONFIG path= " + CAMERACONFIGPATH);
            }
        }

        return CAMERACONFIGPATH;
    }

    public ArrayList<CameraConfig> getList() {

        //System.out.println( "reading config file" );
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            //it is a comment
            if (line.startsWith("#")) {
                continue;
            } else {
                CameraConfig cam = new CameraConfig();
                String[] lineSplit = line.split(",");

                for (int i = 0; i < lineSplit.length; i++) {
                    lineSplit[i] = lineSplit[i].trim();
                    //System.out.println(i+ "="+ lineSplit[i]);
                }

                if (lineSplit.length > 0) {
                    if (lineSplit[4].equals("mjpg")) {
                        //System.out.println("mjpeg connection");
                        cam.setId(lineSplit[0]);
                        //cam.setPvPrefix(lineSplit[1]);
                        cam.setName(lineSplit[2]);
                        cam.setConnectionType(lineSplit[4]);
                        cam.setUrl(lineSplit[5]);

                    } else if (lineSplit[4].equals("epics")) {
                        //System.out.println("epics connection");
                        cam.setId(lineSplit[0]);
                        cam.setPvPrefix(lineSplit[1]);
                        cam.setName(lineSplit[2]);
                        cam.setIocname(lineSplit[3]);
                        cam.setConnectionType(lineSplit[4]);
                        cam.setUrl(lineSplit[5]);

                        //split pv up 
                        String[] temp = lineSplit[1].split(":");
                        cam.P = temp[0] + ":";
                        cam.R = temp[1] + ":";

                    }

                    //System.out.println( cam.toString() );
                    cameras.add(cam);
                    map.put(cam.getId(), cam);

                }

            }
        }

        return cameras;
    }

    public ArrayList<CameraConfig> getXMLList() {

        // First, create a new XMLInputFactory
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        // Setup a new eventReader
        InputStream in;
        try {
            in = new FileInputStream(configFile);
            XMLEventReader eventReader = inputFactory.createXMLEventReader(in);

            // read the XML document
            CameraConfig cam = null;

            while (eventReader.hasNext()) {
                XMLEvent event = eventReader.nextEvent();

                if (event.isStartElement()) {

                    StartElement startElement = event.asStartElement();
//                     System.out.println(startElement.getName().getLocalPart());
//                     System.out.println(startElement.getName().getLocalPart());
                    if (startElement.getName().getLocalPart().equals(CAM)) {
                        cam = new CameraConfig();
//                        System.out.println("New Cam Found");
                    }

                    //System.out.println(event.asStartElement().getName().getLocalPart());
                    if (event.asStartElement().getName().getLocalPart().equals(NAME)) {

                        event = eventReader.nextEvent();
                        cam.setName(event.asCharacters().getData());
//                        System.out.println("Name found = " + cam.getName());
                        continue;
                    }
                    if (event.asStartElement().getName().getLocalPart().equals(ID)) {
                        event = eventReader.nextEvent();
                        cam.setId(event.asCharacters().getData());
//                        System.out.println("ID = " + cam.getId());
                        continue;
                    }
                    if (event.asStartElement().getName().getLocalPart().equals(URL)) {
                        event = eventReader.nextEvent();
                        System.out.println("URL = " + event.asCharacters().asCharacters().getData());
                        cam.setUrl(event.asCharacters().getData());
                        continue;
                    }
                    if (event.asStartElement().getName().getLocalPart().equals(TYPE)) {
                        event = eventReader.nextEvent();
                        cam.setConnectionType(event.asCharacters().getData());
                        continue;
                    }
                    if (event.asStartElement().getName().getLocalPart().equals(IOC)) {
                        event = eventReader.nextEvent();
                        cam.setIocname(event.asCharacters().getData());
                        continue;
                    }
                    if (event.asStartElement().getName().getLocalPart().equals(PV)) {
                        event = eventReader.nextEvent();
                        cam.setPvPrefix(event.asCharacters().getData());
                        continue;
                    }

                }

                if (event.isEndElement()) {
//                    System.out.println(event.asEndElement().getName().getLocalPart());
                    if (event.asEndElement().getName().getLocalPart().equals(CAM)) {
                        cameras.add(cam);
                        map.put(cam.getId(), cam);
                        if (debug) {
                            System.out.println("Number of cams found = " + cameras.size());
                        }
                        if (debug) {
                            System.out.println(cam.toString());
                        }
                    }
                }

            }

        } catch (Exception ex) {
            Logger.getLogger(CameraCollection.class.getName()).log(Level.SEVERE, null, ex);
        }

        return cameras;
    }

    private void parseXmlFile() {
        //get the factory
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {

            //Using factory get an instance of document builder
            DocumentBuilder db = dbf.newDocumentBuilder();

            //parse using builder to get DOM representation of the XML file
            dom = db.parse(configFile);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void parseDocument() {
        //get the root element
        Element docEle = dom.getDocumentElement();

        //get a nodelist of 
        NodeList nl = docEle.getElementsByTagName(CAM);
        if (nl != null && nl.getLength() > 0) {
            for (int i = 0; i < nl.getLength(); i++) {

                //get the employee element
                Element el = (Element) nl.item(i);

                //get the Employee object
                CameraConfig cam = new CameraConfig();
                System.out.println(el.getElementsByTagName(URL).item(0).getNodeValue());

                //add it to list
                cameras.add(cam);
            }
        }
    }

//main for testing this class
    public static void main(String[] args) {
        CameraCollection c = new CameraCollection(ADWindow.ADCONFIG, true);
    }
}
