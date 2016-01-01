package adviewer.util;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;


public class SwingLink extends JLabel {
    private static final long serialVersionUID = 8273875024682878518L;
    private String text;
    private URI uri;
    
    private Cursor cursor = new Cursor( Cursor.HAND_CURSOR );

    public SwingLink() {
        super();
        this.setCursor( cursor );
    }
    
    public SwingLink(String text, URI uri){
        super();
        setup(text,uri);
        this.setCursor( cursor );
    }

    public SwingLink(String text, String uri){
        super();
        this.setCursor( cursor );
        URI oURI;
        try {
            oURI = new URI(uri);
        } catch (URISyntaxException e) {
            // converts to runtime exception for ease of use
            // if you cannot be sure at compile time that your
            // uri is valid, construct your uri manually and
            // use the other constructor.
            throw new RuntimeException(e);
        }
        setup(text,oURI);
    }

    public void setup(String t, URI u){
        text = t;
        uri = u;
        setText(text);
        setToolTipText(uri.toString());
        setForeground(Color.BLUE);
        setOpaque(true);
        
        addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    open(uri);
                }
                public void mouseEntered(MouseEvent e) {
                    //setText(text,false);
                    setForeground(Color.green);
                    setBackground(Color.yellow);
                }
                public void mouseExited(MouseEvent e) {
                    //setText(text,true);
                    setForeground(Color.BLUE);
                    setBackground(null);
                }
        });
    }
    
    public void reSetup(String t, URI u){
        text = t;
        uri = u;
        setText(text);
        setToolTipText(uri.toString());
    }
    
    

    @Override
    public void setText(String text){
        setText(text,true);
    }

    public void setText(String text, boolean ul){
        String link = ul ? "<u>"+text+"</u>" : text;
        super.setText("<html><span style=\"color: #000099;\">"+
                link+"</span></html>");
        this.text = text;
    }

    public URI getUri( ) {
        return uri;
    }

    public URI setStringUri( String uri ) {
        
        URI oURI;
        try {
            oURI = new URI(uri);
            return oURI; 
        } catch (URISyntaxException e) {
            // converts to runtime exception for ease of use
            // if you cannot be sure at compile time that your
            // uri is valid, construct your uri manually and
            // use the other constructor.
            throw new RuntimeException(e);
        } 
    }

    public String getText( ) {
        return text;
    }

    public String getRawText(){
        return text;
    }

    private static void open(URI uri) {
        if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();
                try {
                        if(uri.toString( ).equals( "" ) || uri.toString( ).equals( null )){
                            JOptionPane.showMessageDialog(null,
                                    "No link specified, add a url by clicking the 'Edit' button ",
                                    "Cannot Launch Link",JOptionPane.WARNING_MESSAGE);
                        }
                        else{
                            desktop.browse(uri);
                        }
                } catch (IOException e) {
                    JOptionPane.showMessageDialog(null,
                            "Failed to launch the link, " +
                            "your computer is likely misconfigured.",
                            "Cannot Launch Link",JOptionPane.WARNING_MESSAGE);
                }
        } else {
            JOptionPane.showMessageDialog(null,
                    "Java is not able to launch links on your computer.",
                    "Cannot Launch Link",JOptionPane.WARNING_MESSAGE);
        }
    }
}

