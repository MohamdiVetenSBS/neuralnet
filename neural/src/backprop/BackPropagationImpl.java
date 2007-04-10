package backprop;

/**
 * Author: asalem
 * Description:
 * Date: Feb 13, 2007
 */


import backprop.BackPropagation;

import javax.swing.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class BackPropagationImpl extends Applet implements Runnable, ActionListener {
    private volatile BackPropagation network;
    private volatile double                 training_set[][];
    private final static int                training_set_size       =   10000;//(int)(Math.pow(screen_size,2)*.8);
    private final static int                screen_size             =   500;
    private final static int                circle_radius           =   150;
    private final static int                hidden_units            =   3;
    private final static int                output_length           =   1;
    private final static int                input_length            =   2;
    private final static double             eta                     =   0.1;
    private final static double             alpha                   =   0.01;
    private final static double             accepted_mse            =   20;
    private final static int                max_iterations          =   20000;
    private Thread                          t                       =   null;
    private volatile Graphics               g                       =   null;
    private volatile boolean                running;
    private final boolean                   show_training_set       =   false;
    private final static int                refresh_cycle           =   50;
    private Choice                          choiceBox               =   new Choice();
    private volatile int                    choice                  =   3;
    private volatile Runnable               painter                 =   new Runnable(){
            public void run(){
                int h = screen_size/2;
                g.setColor(Color.white);
                g.fillRect(0,0,screen_size,screen_size);
                g.setColor(Color.black);
                for (int i=0; i<screen_size; i++){
                    for (int j=0; j<screen_size; j++){
                        try{
                            double k = (choiceBox.getSelectedIndex()-1 < 0) ?  network.run(translate(new double[]{i-h,h-j}))[0] : network.runUnit(translate(new double[]{i-h,h-j}), choiceBox.getSelectedIndex()-1)[0];
                            g.setColor(new Color((int)((1-k)*255), (int)((1-k)*255), (int)((1-k)*255)));
                            g.drawLine(i,j,i,j);
                        }catch(Exception e){
                            System.out.println(e.getMessage());
                            System.exit(2);
                        }
                    }
                }
                g.setColor(Color.blue);
                g.drawLine(h,0,h,screen_size); g.drawLine(0,h,screen_size,h);
                if (show_training_set){
                    g.setColor(Color.red);
                    for (int i=0; i<training_set_size; i++)
                        g.drawLine((int)training_set[i][0],(int)training_set[i][1],(int)training_set[i][0],(int)training_set[i][1]);
                }
                add(choiceBox);
            }
        };  

    public void init() {
        initTrainingSet();
        g=this.getGraphics();
        choiceBox.add("All Hidden Units");
        for (int i=0; i<hidden_units; i++){
            choiceBox.add("Hidden Unit "+i);
        }
        add(choiceBox);
        try {
            network = new BackPropagation(input_length,hidden_units,output_length);
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(3);
        }
    }
    public void start(){
        if (t == null){
            t = new Thread(this);
            t.setPriority(Thread.MAX_PRIORITY);
            running = true;
            t.start();
        }
    }
    public void run(){
        if (!running) return;
        if (Thread.currentThread() == t){
               train();
        }
    }
    public void destroy() {
        running = false;
        t = null;
    }

    private void initTrainingSet(){
        training_set = new double[training_set_size][input_length];
        for (int i=0; i<training_set_size; i++){
            for (int j=0; j<input_length; j++){
                training_set[i][j] = (int)(Math.random()*(screen_size/2))*(Math.random() < .5 ? 1 : -1);
            }
        }
    }
    private double[] translate(double[] input){
        double a[] = input.clone();
        for (int i=0; i<a.length; i++)
           a[i] /= screen_size;
        return a;
    }
    private void train(){
        try{
            int i, j, k; double e;
            for (i=0, e=accepted_mse; (i<max_iterations && e>=accepted_mse); i++, System.out.println("Network error:\t"+e+"  \tEpoch: "+i)) {
                for (j=0, k=0, e=0; j<training_set_size; j++, k=(int)(Math.random()*training_set_size)){ /*k for online learning option */
                    e += network.train(translate(training_set[j]),
/*Circle*/              new double[]{(Math.pow(training_set[j][0],2) + Math.pow(training_set[j][1],2) < Math.pow(circle_radius,2)) ? 1 : 0}, //Circle
/*Horizontal*/          //new double[]{((training_set[j][0] < 200)) ? 1 : 0}, //Horizontal
/*Triangle*/            //new double[]{((Math.abs(training_set[j][0]) > training_set[j][1])) ? 1 : 0}, //Horizontal
/*Back Slash*/          //new double[]{(training_set[j][0] + training_set[j][1] <= 100) ? 1 : 0}, //BackSlash
/*Forward Slash*/       //new double[]{(training_set[j][0] >= training_set[j][1]) ? 1 : 0}, //ForwardSlash
/*Bricks*/              //new double[]{(((training_set[j][1] > -100 && training_set[j][1] < -80) || (training_set[j][1] < 100 && training_set[j][1] > 80)) || ((training_set[j][0] > -100 && training_set[j][0] < -80) || (training_set[j][0] < 100 && training_set[j][0] > 80))) ? 1 : 0},
                        eta, alpha);
                }
                if (i % refresh_cycle == 0){
                    SwingUtilities.invokeAndWait(painter);
                }
            } SwingUtilities.invokeAndWait(painter);
        } catch (InterruptedException e) {
            System.out.println(e.toString());
        } catch(Exception ex){
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }

    public void actionPerformed(ActionEvent evt){
        if (evt.getSource() == choiceBox)
            choice = choiceBox.getSelectedIndex()-1;
     }
}
