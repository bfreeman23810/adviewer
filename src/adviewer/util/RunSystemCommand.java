/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adviewer.util;

import java.awt.Choice;
import java.awt.Color;
import java.awt.Panel;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Scanner;
import javax.swing.JLabel;

/**
 *
 * @author root
 */
public class RunSystemCommand implements Runnable {

    public boolean run = true;
    public boolean debug = true;
    public int SLEEPTIME = 1000;
    public JLabel label1;
    public JLabel label2;
    public JLabel label3;
    public JLabel label4;

    public String viewer = "ITVXXXX";
    public String sigmaX = "X.XXXXX";
    public String sigmaY = "X.XXXXX";
    public String ratio = "X:Y";
    private Choice choices;
    private ViewerCollection vc;
    private Panel panel;
    private JLabel newLabel;
    public JLabel messageLabel;

    public RunSystemCommand(boolean debug, int sleeptime) {
        this.debug = debug;
        this.SLEEPTIME = sleeptime;

    }

    public RunSystemCommand(boolean debug, int sleeptime, JLabel label) {
        this.debug = debug;
        this.SLEEPTIME = sleeptime;
        this.label1 = label;
    }

    public void setLabels(JLabel l1, JLabel l2, JLabel l3, JLabel l4) {
        this.label1 = l1;
        this.label2 = l2;
        this.label3 = l3;
        this.label4 = l4;

        setLabelsToEmpty();
    }
    
     public void setLabels(Panel p , JLabel l1, JLabel l2, JLabel l3, JLabel l4) {
        this.label1 = l1;
        this.label2 = l2;
        this.label3 = l3;
        this.label4 = l4;
        this.panel = p;

        setLabelsToEmpty();
    }

    public void setLabelsToEmpty() {
        if (label1 != null) {
            label1.setText("");
        }
        if (label2 != null) {
            label2.setText("");
        }
        if (label3 != null) {
            label3.setText("");
        }
        if (label4 != null) {
            label4.setText("");
        }
    }

    @Override
    public void run() {
        Log.log("Thread Started @ " + SLEEPTIME + " update", debug);
        vc = new ViewerCollection(debug);//build list of viewers
        vc.setLists();

        Viewer v = null;
        Viewer v2 = null;

        newLabel = new JLabel("Select Viewer");
        newLabel.setForeground(Color.white);
        if(messageLabel !=null) messageLabel.setText("DONE building lists....now will monitor insertions...."); 
        
        while (run) {
            try {
                
                
                // String s = SystemCommand.exec("../fakeViewerSigma");
                String s = vc.getInserted().trim();
                Log.log(s, debug);
                
                if (!s.equals("") && s.length() > 0 && s.split(" ").length == 1) {
                    v = vc.viewers.get(s);
                    v2 = null;
                } else if (s.split(" ").length > 1) {
                    v = vc.viewers.get(s.split(" ")[0]);
                    v2 = vc.viewers.get(s.split(" ")[1]);

                }else{
                    setLabelsToEmpty();
                    continue;
                }

                 if(v!=null && label1 !=null && label1.getText().equals(v.name) ){
                    Log.log("Moving on ... " , debug);
                    Thread.sleep(SLEEPTIME);
                    continue;
                }
                else if(v2!=null && label1 != null && label1.getText().equals(v2.name) ){
                    Log.log("Moving on ... " , debug);
                    Thread.sleep(SLEEPTIME);
                    continue;
                }
                
                
                if(v!= null && v2 == null){
                    Log.log("Setting labels ",debug);
                    if(choices != null) panel.remove(choices);
                    if(newLabel != null) panel.remove(newLabel);
                    setLabelStrings(v);
                }
                
                if(v2!=null){
                    if(choices != null) panel.remove(choices);
                    if(newLabel != null) panel.remove(newLabel);
                    
                    choices = new Choice();
                    choices.addItem(v.name);
                    choices.addItem(v2.name);
                    choices.select(v.name);
                    choices.setForeground(Color.white);
                    choices.addItemListener(new ItemListener() {

                        @Override
                        public void itemStateChanged(ItemEvent ie) {
                            Viewer selected = vc.viewers.get( choices.getSelectedItem() );
                            setLabelStrings(selected);
                        }
                    });
                    setLabelStrings(v);
                    panel.add(newLabel);
                    panel.add(choices);
                    
                }
                /**
                 * String s = SystemCommand.exec("../fakeViewerSigma");
                 *
                 * Scanner scanner = new Scanner(s); //Log.log("running",
                 * debug); if (!scanner.hasNext()) { viewer = ""; continue; }
                 *
                 * String str = scanner.nextLine(); if (!str.equals("None")) {
                 * viewer = str.split("=")[1];
                 *
                 * if (!scanner.hasNext()) { sigmaX = ""; continue; } str =
                 * scanner.nextLine(); sigmaX = str.split("=")[1];
                 *
                 * if (!scanner.hasNext()) { sigmaY = ""; continue; } str =
                 * scanner.nextLine(); sigmaY = str.split("=")[1];
                 *
                 * if (!scanner.hasNext()) { ratio = ""; continue; } str =
                 * scanner.nextLine(); ratio = str.split("=")[1]; }*
                 */
                Thread.sleep(SLEEPTIME);

            } catch (Exception e) {
                Log.log("Exeception thrown ... ", debug);
                setLabelsToEmpty();
                run = false;
                if (debug) {
                    e.printStackTrace();
                }
            }
        }

        setLabelsToEmpty();

        Log.log("Stopping get viewer Thread", debug);
        return;

    }

    public void setLabelStrings(Viewer v) {
        if (v != null) {
            if (label1 != null && v.name != null) {
                label1.setText(v.name);
            }
            if (label2 != null && v.sigmaX != null) {
                label2.setText(v.sigmaX);
            }
            if (label3 != null && v.sigmaY != null) {
                label3.setText(v.sigmaY);
            }
            if (label4 != null && v.ratio != null) {
                label4.setText(v.ratio);
            }
        }
    }
    
    public void setLabelStrings(Viewer v , Viewer v2) {
        
        
        
        if (v != null) {
            if (label1 != null && v.name != null) {
                label1.setText(v.name);
            }
            if (label2 != null && v.sigmaX != null) {
                label2.setText(v.sigmaX);
            }
            if (label3 != null && v.sigmaY != null) {
                label3.setText(v.sigmaY);
            }
            if (label4 != null && v.ratio != null) {
                label4.setText(v.ratio);
            }
        }
    }

}
