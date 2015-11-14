/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.image;

import adviewer.util.CameraConfig;
import adviewer.util.Log;
import gov.aps.jca.Channel;
import gov.aps.jca.Context;
import gov.aps.jca.JCALibrary;
import gov.aps.jca.Monitor;
import gov.aps.jca.configuration.DefaultConfiguration;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_Byte;
import gov.aps.jca.dbr.DBR_Float;
import gov.aps.jca.dbr.DBR_Int;
import gov.aps.jca.dbr.DBR_Short;
import gov.aps.jca.event.MonitorEvent;
import gov.aps.jca.event.MonitorListener;
import ij.IJ;
import ij.process.ByteProcessor;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author root
 */
public class EPICSImageStream extends ImageStream implements Stream {

    private String[] pvArray;
    private String colorModePV;
    private String imagePV;
    private String imageIdPV;
    private String zPV;
    private String heightPV;
    private String widthPV;

    private JCALibrary jca;
    private DefaultConfiguration conf;
    private Context ctxt;

    /**
     * these are EPICS channel objects to get images...
     */
    private Channel ch_nx;
    private Channel ch_ny;
    private Channel ch_nz;
    private Channel ch_colorMode;
    private Channel ch_image;
    private Channel ch_image_id;
    private ArrayList<Channel> channels;
    private Monitor idMonitor;
    private int events = 0;
    private volatile int UniqueId = 0;
    private boolean isConnected;
    private boolean isDebugMessages = false;

    int x = 0;
    int y = 0;
    int z = 0;

    private String notConnectedChannel;
    private adviewer.image.EPICSImageStream.newUniqueIdCallback cb;
    private int prevUniqueId;
    private DBRType dt;
    private Object nz;
    private Object cm;
    private byte[] imageBytes;
    private ByteArrayInputStream bais;

    private BufferedImage image;
    private byte[] pixels;
    
    public int SLEEPTIME = 5;

    public EPICSImageStream(CameraConfig cam, boolean debug) {
        super();
        super.debug = debug;
        super.cam = cam;
        super.processing = true;

        channels = new ArrayList<Channel>();
        //setSleepTime(TWENTY_FIVE_FPS);

        if (cam.getPvPrefix() == null || cam.getPvPrefix().equals("")) {

            Log.log("Error ... no PV declared", super.debug);

            System.exit(1);
        } else {

            try {
                //connect CA
                startEPICSCA();
                connectPVs(cam.getPvPrefix(), true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "There was a problem with opening channel : " + cam.getPvPrefix(), "Error", JOptionPane.ERROR_MESSAGE);

                Log.log("Error in channel ... closing \n ", super.debug);
                if (super.debug) {
                    e.printStackTrace();
                }
                System.exit(1);
            }

            getValuesFromEpics();

            //super.impp = new ImagePlusPlus(cam.getName(), new ByteProcessor( imageWidth, imageHeight), this);
            //super.impp.showMe();
            //super.show = true;
            //updateImpp();
            Log.log(".... continuing ...", super.debug);

        }
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void kill() {
        super.stop();

    }

    public void destroy() {
        try {
            this.closeEPICSCA();
            this.disconnectPVs();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub

        Log.log("In run ... ", super.debug);

        while (super.processing && checkConnections() && cb.isNewImageAvailable) {
            //Log.log("running ... " , super.debug)
            getValuesFromEpics();
            

            checkSizes();
            
            //updateImpp();
            //print("Running + >>>>> ");
            //need to change img
            //byte[] img = null;

            try {
                //img = epicsGetByteArray(ch_nx, x);
                byte[] img = (byte[]) epicsGetByteArray(ch_image, imageSize);

                if (img != null && img.length == 0) {
                    Log.log("img is null .... ", super.debug);
                    break;

                } else {
                    updateImage(img);
                }

            } catch (Exception ex) {

                Log.log("problem creating image from epics array ...\n" + ex.getMessage(), super.debug);

            }

            //String ctype;
            //
            // FPS counter.
            //
            //if (m_imgidx > IMG_FLUFF_FACTOR && m_startTime == 0) {
            //m_startTime = System.currentTimeMillis();
            //}
            //
            // Update the image [fores events off]
            //
            try {
                Thread.sleep(this.SLEEPTIME);
            } catch (Exception e) {
                System.err.println("Error in putting thread to sleep .... ");
                e.printStackTrace();
            }

        }

    }

    @Override
    public ImagePlusPlus updateImpp() {
        // TODO Auto-generated method stub
        try {

            if (super.impp.getWindow() != null && !super.impp.isClosed()) {
                pixels = (byte[]) super.impp.getProcessor().getPixels();
                // if(isDebugMessages) IJ.log("pixel val [0] [1] pre update = " + pixels[0] + " , " + pixels[1] );
                pixels = epicsGetByteArray(ch_image, super.imageSize);

                if (this.isSubtraction & savedPixelBytes != null && savedPixelBytes.length > 0) {
                    pixels = subtraction(pixels);
                }

                super.impp.getProcessor().setPixels(pixels);
                super.impp.updateAndDraw();

                //if stacking
                if (super.isSaveToStack) {
                    super.impp.getStack().addSlice(cam.getName(), super.impp.getProcessor().duplicate());
                }

                super.impp.setSlice(super.impp.getNSlices());
                super.impp.showMe();

                cb.isNewImageAvailable = false; //monitor will set this back to true when a new update comes in
                super.numImageUpdates++;
                return super.impp;
            } else {
                Log.log("Window is null...stopping ",debug);
                

                if (!super.show) {
					//getImpp().close();

                    //connect CA, because a clsoing window event has destroyed 
                    //the Connection;
                    //startEPICSCA();
                    //connectPVs(cam.getPvPrefix(), false);
                    getValuesFromEpics();

                    super.impp = new ImagePlusPlus(cam.getName(), new ByteProcessor(super.imageWidth, super.imageHeight), this);
                    super.impp.showMe();

                    super.show = true;

                    return super.impp;
                } else {
                    System.exit(1);
                }

            }

        } catch (Exception e) {
            if (debug) {
                System.err.println("Problem with processing the new pixels ... ");
                e.printStackTrace();
            }
            this.stop();
            System.exit(1);

        }

        return super.impp;
    }

    /**
     * Get values from epics channels, also set the image width and height in
     * ImageStream.
     */
    public void getValuesFromEpics() {

        try {
            super.isConnected = checkConnections();
            if (!super.isConnected) {
                System.err.println("EPICS Channels are not connected...");
                return;
            }
            //get x,y,z values, colormode, and datatype from the epics channels, use referenced instance of ADEpics()
            super.imageWidth = epicsGetInt(ch_nx);
            super.imageHeight = epicsGetInt(ch_ny);
            super.imageSize = (super.imageWidth * super.imageHeight);

            nz = epicsGetInt(ch_nz);
            cm = epicsGetInt(ch_colorMode);
            this.prevUniqueId = UniqueId;
            UniqueId = UniqueId;
            super.frameId = UniqueId;
            dt = ch_image.getFieldType();
        } catch (Exception e) {
            System.err.println("Error in getting EPICS variables ... ");
            e.printStackTrace();
        }
    }

    /**
     * connect the epics channels
     *
     * @String - PVPrefix to use
     * @boolean - true if you want the program to exit upon no counting image
     * updates
     *
     *
     */
    public void connectPVs(String PVPrefix, boolean exitOnNoUpdate) {

        /**
         * Channel Names
         */
        String widthPV = PVPrefix + "ArraySize0_RBV";
        String heightPV = PVPrefix + "ArraySize1_RBV";
        String zPV = PVPrefix + "ArraySize2_RBV";
        String colorModePV = PVPrefix + "ColorMode_RBV";
        String imagePV = PVPrefix + "ArrayData";
        String imageIdPV = PVPrefix + "UniqueId_RBV";

        try {
           // PVPrefix = PVPrefixText.getText();

            //logMessage("Trying to connect to EPICS PVs: " + PVPrefix, true, true);
            if (super.debug) {
                System.out.println("Trying to connect to EPICS PVs: " + PVPrefix + "....");
            }

            //connect epics channels
            ch_nx = createEPICSChannel(widthPV);
            channels.add(ch_nx);
            ch_ny = createEPICSChannel(heightPV);
            channels.add(ch_ny);
            ch_nz = createEPICSChannel(zPV);
            channels.add(ch_nz);
            ch_colorMode = createEPICSChannel(colorModePV);
            channels.add(ch_colorMode);
            ch_image = createEPICSChannel(imagePV);
            channels.add(ch_image);
            ch_image_id = createEPICSChannel(imageIdPV);
            channels.add(ch_image_id);

            x = epicsGetInt(ch_nx);
            y = epicsGetInt(ch_ny);
            z = epicsGetInt(ch_nz);

            //areaDetector pins a UNique Id to a new image. A CA monitor is setup on this PV
            //updates to image will occur when a change to this monitor occurs
            cb = new newUniqueIdCallback();
            idMonitor = ch_image_id.addMonitor(Monitor.VALUE, cb);

            ctxt.flushIO();

            if (checkConnections()) {
                if (debug) {
                    System.out.println("Epics Channels are connected .... ");
                }

                if (debug) {
                    System.out.println("Image Id = " + UniqueId);
                }

                if (UniqueId == 0) {
                    String message = "Something is wrong with the Image Counter. Check the Image Plugin EDM screen.";
                    message += "\n Channel = " + imagePV;
                    JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);

                    if (exitOnNoUpdate) {
                        if (debug) {
                            System.err.println("Exiting .... EPICSImageUpdater.... line 341 ... ");
                        }
                        System.exit(1);
                    }
                }
            } else {
                if (debug) {
                    System.out.println("Epics Channels are not connected ..." + notConnectedChannel);
                }
            }
        } catch (Exception ex) {

            if (debug) {
                System.err.println("Error in connecting PV...");
                ex.printStackTrace();
            }
            JOptionPane.showMessageDialog(null, "There was a problem with opening channel : " + PVPrefix, "Error", JOptionPane.ERROR_MESSAGE);
            this.stop();
            System.exit(1);
            //checkConnections();
        }
    }

    private boolean checkConnections() {
        // TODO Auto-generated method stub
        try {
            for (Channel c : channels) {
                if (c == null || !checkChannel(c)) {
                    if (debug) {
                        print(c.getName() + "...... line 396 in EPICSImageStreamer");
                    }
                    return false;
                }
            }
        } catch (Exception ex) {

            // IJ.log("checkConnections: got exception= " + ex.getMessage());
            //if (isDebugFile) ex.printStackTrace(debugPrintStream);
            ex.printStackTrace();
            return false;
        }
        //set the Unique Id
        try {
            UniqueId = epicsGetInt(ch_image_id);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return true;
    }

    public void disconnectPVs() {
        try {
            if (debug) {
                print("disconnecting PVs ... ");
            }
            for (Channel c : channels) {
                c.destroy();
            }
            isConnected = false;
            //logMessage("Disconnected from EPICS PVs OK", true, true);
        } catch (Exception ex) {
            //logMessage("Cannot disconnect from EPICS PV:" + ex.getMessage(), true, true);
        }
    }

    public void startEPICSCA() {
        //logMessage("Initializing EPICS", true, true);

        try {
            System.setProperty("jca.use_env", "true");
            // Get the JCALibrary instance.
            jca = JCALibrary.getInstance();
            ctxt = jca.createContext(JCALibrary.CHANNEL_ACCESS_JAVA);
            ctxt.initialize();
        } catch (Exception ex) {
            if (debug) {
                System.err.println("startEPICSCA exception: " + ex.getMessage());
            }
            //logMessage("startEPICSCA exception: " + ex.getMessage(), true, true);
        }

    }

    public void closeEPICSCA() throws Exception {
        //if(isDebugMessages) IJ.log("Closing EPICS");
        //logMessage("Closing EPICS", true, true);
        ctxt.destroy();
    }

    public Channel createEPICSChannel(String chname) throws Exception {

        try {
            // Create the Channel to connect to the PV.
            Channel ch = ctxt.createChannel(chname);

            // send the request and wait for the channel to connect to the PV.
            ctxt.pendIO(3.0);

            /*if (isDebugFile)
             {
             debugPrintStream.print("\n\n  Channel info****************************\n");
             ch.printInfo(debugPrintStream);
             }
             */
            if (debug) {
                print("**************** Channel Info **************************************");
                print("Host is " + ch.getHostName());
                print("can read = " + ch.getReadAccess());
                print("can write " + ch.getWriteAccess());
                print("type " + ch.getFieldType());
                print("name = " + ch.getName());
                print("element count = " + ch.getElementCount());
            }
            return (ch);

        } catch (Exception e) {
            notConnectedChannel = chname;
            throw e;
        }

    }

    public class newUniqueIdCallback implements MonitorListener {

        public boolean isNewImageAvailable;

        @Override
        public void monitorChanged(MonitorEvent ev) {
            //if (isDebugMessages)
            //  IJ.log("Monitor callback");
            isNewImageAvailable = true;
            DBR_Int x = (DBR_Int) ev.getDBR();
            UniqueId = (x.getIntValue())[0];
            events++;
            //Log.log("changed ", true);
            // I'd like to just do the synchronized notify here, but how do I get "this"?
            newUniqueId(ev);
        }

    }

    public void newUniqueId(MonitorEvent ev) {
        synchronized (this) {
            notify();
        }
    }

    public boolean checkChannel(Channel ch) {
        if ((ch != null) && ch.getConnectionState() == Channel.ConnectionState.CONNECTED) {

            return true;
        }

        notConnectedChannel = ch.getName();
        return false;

    }

    /**
     * if size changes, then reinstatiate a new imageplusplus obj
     */
    public void checkSizes() {

        if (x != super.imageWidth || y != super.imageHeight) {
            x = super.imageWidth;
            y = super.imageHeight;
            super.impp.close();
            super.impp = new ImagePlusPlus(cam.getName(), new ByteProcessor(super.imageWidth, super.imageHeight), this);
            super.impp.showMe();
        }

    }

    public int epicsGetInt(Channel ch) throws Exception {
        // if (isDebugMessages)
        // IJ.log("Channel Get: " + ch.getName());
        DBR_Int x = (DBR_Int) ch.get(DBRType.INT, 1);
        ctxt.pendIO(1.0);
        return (x.getIntValue()[0]);
    }

    public byte[] epicsGetByteArray(Channel ch, int num) throws Exception {
        DBR x = ch.get(DBRType.BYTE, num);
        ctxt.pendIO(1.0);
        DBR_Byte xi = (DBR_Byte) x;
        byte zz[] = xi.getByteValue();
        return (zz);
    }

    public short[] epicsGetShortArray(Channel ch, int num) throws Exception {
        DBR x = ch.get(DBRType.SHORT, num);
        ctxt.pendIO(1.0);
        DBR_Short xi = (DBR_Short) x;
        short zz[] = xi.getShortValue();
        return (zz);
    }

    public float[] epicsGetFloatArray(Channel ch, int num) throws Exception {
        DBR x = ch.get(DBRType.FLOAT, num);
        ctxt.pendIO(1.0);
        DBR_Float xi = (DBR_Float) x;
        float zz[] = xi.getFloatValue();
        return (zz);
    }

    public static void print(String s) {
        System.out.println(s);
    }

    @Override
    public void updateImage() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public byte[] getRawImage() {
        // TODO Auto-generated method stub
        return super.rawImage;
    }

    @Override
    public Image getCurrent() {
        // TODO Auto-generated method stub
        return super.img;
    }

    @Override
    public void fireImageChange() {
        // TODO Auto-generated method stub
        super.fireImageChange();
    }

    private void updateImage(byte[] img) {
        //
        // Update our image...
        //
        //super.imageType = ctype;
        if (img == null) {
            Log.log(" IMG is null ....  ", super.debug);
            return;
        }

        super.img = super.tk.createImage(img);
        super.rawImage = img;
        super.imgidx++;

        //System.out.println( this.cam.getName() + "");
        //ImagePlusPlus impp2 = new ImagePlusPlus(this.mycam.getName() , super.img);
        //super.updateImpp(this.impp , impp2);
        //super.impp = new ImagePlusPlus( "test" , super.img , this );
        super.img.getWidth(new ImageObserver() {
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                boolean fully = ((infoflags & (ImageObserver.ALLBITS | ImageObserver.PROPERTIES)) != 0);
                //if (fully) {
                fireImageChange();
                //}
                return !fully;
            }
        });

    }

    public void finalize() throws Throwable {
        unhook();
        super.finalize();
    }

    public void unhook() {
        super.collecting = false;
        super.isDefunct = true;
        try {
            if (super.inputStream != null) {
                super.inputStream.close();
            }
            super.inputStream = null;
        } catch (Exception ignored) {
        }
    }

    @Override
    public ImagePlusPlus getImagePlusPlus() {

        return super.impp;
    }

}
