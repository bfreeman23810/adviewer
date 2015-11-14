package adviewer.image;

import java.awt.Image;

public interface Stream {
	
	public  void updateImage();
	public  void run();
	public  byte[] getRawImage();
	public  Image getCurrent();
        public  ImagePlusPlus getImagePlusPlus();
	public  void fireImageChange();
	public  void destroy();
	
	
	
}
