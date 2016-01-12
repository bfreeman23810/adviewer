package adviewer.image;

import java.awt.Image;
import java.awt.image.ImageObserver;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.Hashtable;

import adviewer.util.CameraConfig;
import adviewer.util.Log;

import com.charliemouse.cambozola.shared.StreamSplit;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;

/**
 * adviewer.image.MJPGImageStream.java 
 * This
 * 
 * @author bfreeman
 */
public class MJPGImageStream extends ImageStream implements Stream {

    public static final int CONNECT_STYLE_SOCKET = 1;
    public static final int CONNECT_STYLE_HTTP = 2;
    private static final int IMG_FLUFF_FACTOR = 1;
    public CameraConfig mycam;
    private String BC_UNAME = "alarms";
    private String BC_PW = "alarms1";
    public int SLEEPTIME = 2;

    public MJPGImageStream(CameraConfig cam, boolean debug) {
        super(cam);
        this.mycam = cam;
        super.cam = cam;
        this.debug = debug;

        if (!cam.getConnectionType().equals("mjpg")) {
            Log.log("Type not mjpg ... look at config file and set stream type", debug);
            return;
        }
        if (cam.getUrl() == null || cam.getUrl().equals("")) {
            Log.log("Please set the URL in the config .... exiting ... ", debug);
            System.exit(1);
        } else {
            try {
                super.stream = new URL(cam.getUrl());
            } catch (MalformedURLException ex) {
                //Logger.getLogger(MJPGImageStream.class.getName()).log(Level.SEVERE, null, ex);
                Log.log("MJPGImageStream throws ex .... URL problem ....  line 39 \n" + ex.getMessage(), debug);

            }
        }
    }

    public void updateImage() {
        // TODO Auto-generated method stub
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

    @Override 
    public void kill(){
        super.kill();
        
    }
    
    @Override
    public void run() {
        MJPEGStreamSplit ssplit;

        try {
            //
            // Loop for a while until we either give up (hit m_retryCount), or
            // get a connection.... Sleep inbetween.
            //
            String connectionError;
            String ctype;
            Hashtable headers;
            int tryIndex = 0;
            int retryCount = super.retryCount;
            int retryDelay = super.retryDelay;
            super.show = true;

            //
            do {

                //
                // Keep track of how many times we tried.
                //
                tryIndex++;

                if (super.debug) {
                    System.err.println("// Connection URL = " + super.stream);
                }
                //          
                // Better method - access via URL Connection
                //

                URLConnection conn;

                if (isBlueCherry(stream)) {
                    conn = connectToBlueCherry();
                } else {
                    conn = super.stream.openConnection();

                    if (super.docBase != null) {
                        conn.setRequestProperty("Referer", super.docBase.toString());
                    }

                    conn.setRequestProperty("User-Agent", super.appName);
                    conn.setRequestProperty("Host", super.stream.getHost());

                    if (super.userpassEncoded != null) {
                        conn.setRequestProperty("Authorization", "Basic " + super.userpassEncoded);
                    }
                }

                super.inputStream = new DataInputStream(new BufferedInputStream(conn.getInputStream()));

                //
                // Read Headers for the main thing...
                //
                headers = MJPEGStreamSplit.readHeaders(conn);
                ssplit = new MJPEGStreamSplit(super.inputStream);
                //
                if (super.debug) {
                    System.err.println("// Request sent; Main Response headers:");
                    for (Enumeration enm = headers.keys(); enm.hasMoreElements();) {
                        String hkey = (String) enm.nextElement();
                        System.err.println("//   " + hkey + " = " + headers.get(hkey));
                    }
                }

                super.collecting = true;
                //
                // Work out the content type/boundary.
                //
                connectionError = null;
                ctype = (String) headers.get("content-type");
                if (ctype == null) {
                    connectionError = "No main content type";
                } else if (ctype.indexOf("text") != -1) {
                    String response;
                    //noinspection deprecation
                    while ((response = super.inputStream.readLine()) != null) {
                        System.out.println(response);
                    }
                    connectionError = "Failed to connect to server (denied?)";
                }

                if (connectionError == null) {
                    break; // Yay!! got one.
                } else if (super.isDefunct) {
                    //
                    // Not wanted any more...
                    //
                    return;
                } else {
                    //
                    // Wait a while before retrying...
                    //
                    if (super.debug) {
                        System.err.println("// Waiting for " + retryDelay + " ms");
                    }

                    super.reporter.reportFailure(connectionError);
                    sleep(retryDelay);
                }
            } while (tryIndex < retryCount); //end do -> while
            //
            if (connectionError != null) {
                return;
            }
            //
            // Boundary will always be something - '--' or '--foobar'
            //
            int bidx = ctype.indexOf("boundary=");
            String boundary = StreamSplit.BOUNDARY_MARKER_PREFIX;
            if (bidx != -1) {
                boundary = ctype.substring(bidx + 9);
                ctype = ctype.substring(0, bidx);
                //
                // Remove quotes around boundary string [if present]
                //
                if (boundary.startsWith("\"") && boundary.endsWith("\"")) {
                    boundary = boundary.substring(1, boundary.length() - 1);
                }
                if (!boundary.startsWith(MJPEGStreamSplit.BOUNDARY_MARKER_PREFIX)) {
                    boundary = MJPEGStreamSplit.BOUNDARY_MARKER_PREFIX + boundary;
                }
            }

            //
            // Now if we have a boundary, read up to that.
            //

            if (ctype.startsWith("multipart/x-mixed-replace")) {
                if (super.debug) {
                    System.err.println("// Reading up to boundary");
                }
                ssplit.skipToBoundary(boundary);
            }

            do {
                //System.out.println("back up top of stream.... ");
                if (super.collecting) {
                    //
                    // Now we have the real type...
                    //  More headers (for the part), then the object...
                    //
                    if (boundary != null) {
                        headers = ssplit.readHeaders();

                        if (super.debug) {
                            //System.err.println("// Chunk Headers recieved:");
                            for (Enumeration enm = headers.keys(); enm.hasMoreElements();) {
                                String hkey = (String) enm.nextElement();
                                //System.err.println("//   " + hkey + " = " + headers.get(hkey));
                            }
                        }
                        //
                        // Are we at the end of the m_stream?
                        //
                        if (ssplit.isAtStreamEnd()) {
                            break;
                        }
                        ctype = (String) headers.get("content-type");
                        if (ctype == null) {
                            throw new Exception("No part content type");
                        }
                    } //end if boundry null

                    //
                    // Mixed Type -> just skip...
                    //
                    if (ctype.startsWith("multipart/x-mixed-replace")) {
                        //
                        // Skip
                        //
                        bidx = ctype.indexOf("boundary=");
                        boundary = ctype.substring(bidx + 9);
                        //
                        if (super.debug) {
                            System.err.println("// Skipping to boundary");
                        }
                        ssplit.skipToBoundary(boundary);
                    } else {
                        //
                        // Something we want to keep...
                        //
                        if (super.debug) {
                            // System.err.println("// Reading to boundary");
                        }
                        byte[] img = ssplit.readToBoundary(boundary);

                        if (img.length == 0) {
                            break;
                        }
                        //
                        // FPS counter.
                        //
                        //if (m_imgidx > IMG_FLUFF_FACTOR && m_startTime == 0) {
                        //m_startTime = System.currentTimeMillis();
                        //}
                        //
                        // Update the image [fores events off]
                        //
                        updateImage(ctype, img);
                    } //end if multi - else ..... 
                }

                try { //now thread can sleep for a few
                    Thread.sleep(this.SLEEPTIME);
                    //System.out.println("Done .... ");
                    //System.exit(0);
                } catch (InterruptedException ignored) {
                }
            } while (!super.isDefunct); //end second do -> while
        } catch (Exception e) {
            if (!super.collecting) {
                //super.reporter.reportFailure(e.toString());

                e.printStackTrace();
            } else if (!super.isDefunct) {
                //super.reporter.reportError(e);
                e.printStackTrace();
            }
        } finally {
            unhook();
            Log.log( "MJPEGStream has been disconnected ... " , debug);
        }
        //
        // At this point, the m_stream m_inputStream done
        // [could dispplay a that's all folks - leaving it as it m_inputStream
        //  will leave the last frame up]
        //
        
        
    }

    private void updateImage(String ctype, byte[] img) {
        //
        // Update our image...
        //
        super.imageType = ctype;
        super.img = super.tk.createImage(img);
        super.rawImage = img;
        super.imgidx++;

        // System.out.println( this.mycam.getName() + "");


        //ImagePlusPlus impp2 = new ImagePlusPlus(this.mycam.getName() , super.img);
        //super.updateImpp(this.impp , impp2);
        //super.impp = new ImagePlusPlus( "test" , super.img , this );

        super.img.getWidth(new ImageObserver() {
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                boolean fully = ((infoflags & (ImageObserver.ALLBITS | ImageObserver.PROPERTIES)) != 0);
                if (fully) {
                    fireImageChange();
                }
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

    public boolean isBlueCherry(URL url) {

        if (super.debug) {
            print("Server parms : \n");
            print("Protocol:" + url.getProtocol());
            print("File: " + url.getFile());
            print("Host: " + url.getHost());
            print("Query: " + url.getQuery());
            print("Port: " + url.getPort() + "");
            print("Path: " + url.getPath());
        }

        if (url.getProtocol().equals("https") && url.getHost().equals("opsdvr")) {
            super.isCEBAFBC = true;
            Log.log("is CEBAF bluecherry .. ", super.debug);
            return true;

        }

        if (url.getProtocol().equals("https") && url.getHost().equals("feldvr")) {

            super.isLERFBC = true;
            Log.log("is LERF bluecherry .. ", super.debug);

            return true;

        }
        return false;
    }

    public void print(String s) {
        System.out.println(s);
    }

    /**
     * Connect to secure server. 
     * Need to make this more generic .... and put uname and pw in config file
     * @return 
     */
    public HttpsURLConnection connectToBlueCherry() {
        HttpsURLConnection conn = null;
        if (super.stream == null) {
            if (super.debug) {
                System.err.println("no URL defined ..... conn is returned as null....adviewer.MJPGImageStream .... line 418");
            }
            return conn;
        }
        try {
            // configure the SSLContext with a TrustManager
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(new KeyManager[0], new TrustManager[]{new DefaultTrustManager()}, new SecureRandom());
            SSLContext.setDefault(ctx);

            Base64 b = new Base64();
            String encoding = b.encodeAsString(new String(this.BC_UNAME + ":" + this.BC_PW).getBytes());

            if (super.stream == null) {
                System.err.println("url is null ... No connection");
                return null;
            }

            conn = (HttpsURLConnection) super.stream.openConnection();

            //this overrides host name verifier
            conn.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });

            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Authorization", "Basic " + encoding);


            //serverResponse = conn.getResponseCode();
            //setServerResponse(conn.getResponseCode());
            // System.out.println("Response Code from Server : "+ serverResponse);

            //inputStream =  (InputStream) conn.getInputStream();

            //BufferedReader in   =  new BufferedReader (new InputStreamReader (inputStream));
            //   String line;
            //while ((line = in.readLine()) != null) {
            //System.out.println(line);
            //}

        } catch (Exception e) {
            e.printStackTrace();
            return conn;
        }

        if (super.debug) {
            print("Connected to Blue Cherry Server ...... ");
        }

        return conn;

    }

    private static class DefaultTrustManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
