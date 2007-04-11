package backprop.AutoEncoders;

import backprop.BackPropagation;

import javax.swing.*;
import java.util.Arrays;
import java.awt.*;
import java.applet.Applet;

public class AutoEncoderPoints extends Applet implements Runnable {
    private static BackPropagation          network;
    private static double                   training_set[][];
    private final static int                screen_size             =   500;
    private final static int                training_set_size       =   12;
    private final static int                hidden_units            =   2;
    private final static int                output_length           =   12;
    private final static int                input_length            =   12;
    private final static double             eta                     =   0.1;
    private final static double             alpha                   =   0.01;
    private final static double             accepted_mse            =   0.4;
    private final static int                max_iterations          =   1000000;
    private static Graphics                 g                       =   null;
    private Thread                          t                       =   null;
    private volatile boolean                running                 =   false;
    private static Runnable                 painter                 =   new Runnable(){
            public void run(){
                double points[][] = new double[training_set_size][2];
                g.setColor(Color.white);
                g.fillRect(0,0,screen_size,screen_size);
                for (int i=0; i<training_set_size; i++){
                    double k[];
                    try{
                        k = network.run(training_set[i]);
                    }catch(Exception e){
                        System.out.println(e.getMessage());
                        System.exit(20);
                    }
                    points[i] = network.getHiddenLayer();
                    points[i][0] *= screen_size; points[i][1] *= screen_size;
                    g.setColor(Color.black);
                    g.fillOval((int)points[i][0]-5, (int)points[i][1]-5, 10, 10);
                }
            }
        };


    public void init() {
         try {
            initTrainingSet();
            g =this.getGraphics();
            g.setColor(Color.white);
            g.fillRect(0,0, screen_size, screen_size);
            network = new BackPropagation(input_length,hidden_units,output_length);
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(3);
        }
//        double k[] = generate_inputPatterns(1,0);
//        for (int i=0; i<k.length; i++){
//            if (i%graph_axis_size == 0)
//                System.out.print("\n");
//            System.out.print((int)k[i]);
//        }
//        System.exit(0);
    }
    public void start() {
        if (t == null){
            t = new Thread(this);
            t.setPriority(Thread.MAX_PRIORITY);
            running = true;
            t.start();
        }
    }
    public void run(){
         try {
             if (!running) return;
             train();
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(3);
        }
        print();
    }

    private static void initTrainingSet(){
        training_set = new double[training_set_size][input_length];
        for (int i=0; i< training_set_size; i++){
            for (int j=0; j< input_length; j++){
                training_set[i][j] = (j==i ? 1 : 0);
            }
        }
    }
    private static void train(){
        try{
            int i, j, k; double e;
            for (i=0, e= accepted_mse; (i< max_iterations && e>= accepted_mse); i++, System.out.println("Network error:\t"+e+"  \tEpoch: "+i), SwingUtilities.invokeAndWait(painter)) {
                for (j=0, k=0, e=0; j< training_set_size; j++, k=(int)(Math.random()* training_set_size)){ /*k for online learning option */
                    e += network.train(training_set[j], training_set[j], eta, alpha);
                }
            }
        } catch(Exception ex){
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }
    private static void print(){
        int success_rate=0;
        try{
            for (int i=0; i< training_set_size; i++) {
                for (int j=0; j< input_length; j++)
                    System.out.print((int) training_set[i][j]+" ");
                System.out.print("\t-->\t");
                double output[]= network.run(training_set[i]);
                int success=0;
                double k[] = new double[input_length];
                for(int j=0; j< input_length; j++){
                    k[j]=output[j]>0.5?1:0;
                    System.out.print((output[j]>0.5?1:0)+" ");
                }
                System.out.print("\n");
                if (Arrays.equals(k, training_set[i]))
                    success_rate++;
            }
            System.out.print("Reproduced "+success_rate+" patterns out of "+ training_set_size);
        } catch(Exception ex){
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }
}
