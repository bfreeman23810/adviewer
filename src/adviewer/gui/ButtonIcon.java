/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.gui;

import adviewer.util.Log;
import static ij.IJ.URL;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JToggleButton;

/**
 *
 * @author root
 */
public class ButtonIcon extends JToggleButton {

    public ImageIcon icon1;
    public ImageIcon icon2;
    public boolean toggle = false;
    public boolean debug = true;

    public ButtonIcon(String title, ImageIcon ico1, ImageIcon ico2) {
        super(title);
        this.icon1 = ico1;
        this.icon2 = ico2;
        super.setIcon(ico1);
        //super.repaint();
    }

    public ButtonIcon(ImageIcon ico1, ImageIcon ico2) {
        super(ico1);
        this.icon1 = ico1;
        this.icon2 = ico2;
        super.setIcon(ico1);
        super.setSelectedIcon(ico2);
       
    }
    
    public ButtonIcon(String path1, String path2) {
        super();
        setImageIcon(path1, path2);
    }

    public void toggleIcon() {
        if (toggle) {
            toggle = false;
            //super.setIcon(this.icon2);
        } else {
            toggle = true;
            //super.setIcon(this.icon1);
        }
        super.repaint();
    }

    public void reset() {
        super.setSelected(false);
    }

    @Override
    public void setSelected(boolean b) {
        if (!isSelected()) {
            super.setSelected(b);
        }
    }

    public void setImageIcon(String path1, String path2) {
        try {
            //URL file = getClass().getResource(path1);
            //URL file2 = getClass().getResource(path2);
            //BufferedImage img = ImageIO.read(file);
            //BufferedImage img2 = ImageIO.read(file2);

            ImageIcon icon = new ImageIcon(path1);
            ImageIcon icon2 = new ImageIcon(path2);
            this.setIcon(icon);
            this.setSelectedIcon(icon2);
        } catch (Exception ioex) {
            Log.log("load error: " + ioex.getMessage(), debug);
        }

    }

}
