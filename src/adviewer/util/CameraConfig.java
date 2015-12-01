package adviewer.util;

import java.awt.Image;

/**
 * data class to store config data about separate cameras
 * @author bfreeman
 *
 */

public class CameraConfig {

	private String id;
	private String name;
	private String pvPrefix;
	public String P;
	public String R;
	private String host;
	private String iocname;
	private String url;
	private String connectionType;
        private String pathToDefaultImage;
        private Image defaultImg;
 	private boolean debug = true;
        
        public static final String MJPG = "mjpg";
        public static final String STATIC = "static";
        public static final String EPICS = "epics";
        public static final String RTSP = "rtsp";
        
        public static final String[] CONNECTIONS = {MJPG , STATIC, EPICS, RTSP }; 
        
	public CameraConfig(){
		
	}
	
        public CameraConfig( String id , String name ){
		setId(id);
		setName(name);
		setConnectionType(STATIC);
	}
        
	public CameraConfig( String id, String name , String pvPrefix ){
		this.setId(id);
		this.setName(name);
		this.setPvPrefix(pvPrefix);
	}

	
	public CameraConfig( String id, 
			String name , 
			String pvPrefix,
			String url,
			String connectionType){
		this.setId(id);
		this.setName(name);
		this.setPvPrefix(pvPrefix);
		this.setConnectionType(connectionType);
		this.setUrl(url);
	}
	
	public CameraConfig( String id, 
			String name , 
			String url,
			String connectionType){
		this.setId(id);
		this.setName(name);
                this.setUrl(url);
		this.setConnectionType(connectionType);
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPvPrefix() {
		return pvPrefix;
	}

	public void setPvPrefix(String pvPrefix) {
		this.pvPrefix = pvPrefix;
	}

	public String getIocname() {
		return iocname;
	}

	public void setIocname(String iocname) {
		this.iocname = iocname;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getId() {
		return id;
	}

	public void setId(String id2) {
		this.id = id2;
	}

	public String getConnectionType() {
		return connectionType;
	}

	public void setConnectionType(String connectionType) throws IllegalArgumentException{
		
            boolean found = false;
            String types = "";
            for(String s: CONNECTIONS){
                types +=s+"\n";
                if( connectionType.equals(s)){
                    found = true;
                    
                }
            }
                if(found == false){
                    Log.log("Unsupported camera connection type!"
                            + "+\nSupported type are "
                            + "\n"+types,debug);
                    throw new IllegalArgumentException("Invalid Type in Camera Configuration");
                }	
            
		if(connectionType.equals("epics") && pvPrefix==null){
			Log.log(":"+connectionType+": defined with no pv",debug);
			throw new IllegalArgumentException("epics is defined with no pv, please see config file");
		}
		
                if(connectionType.equals("mjpg") && url==null){
			Log.log(":"+connectionType+": defined with no url" , debug);
			throw new IllegalArgumentException("mjprg stream defined with no url");
		}
                
		this.connectionType = connectionType;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public boolean equals(Object arg0) {
		// TODO Auto-generated method stub
		return super.equals(arg0);
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String s = "";
		s+="[ ";
		s+="id = "+ getId();
                s+=", pv = " + getPvPrefix();
		s+=", name = " + getName();
		s+=", connection = " + getConnectionType();
		if(this.url!=null){
			s+=", url = " + getUrl();
		}
		s+=" ]";
		return s;
	}
	
	
	
	
	
	
}
