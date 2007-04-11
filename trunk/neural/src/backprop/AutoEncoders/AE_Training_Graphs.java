package backprop.AutoEncoders;

import backprop.BackPropMultiHiddenLayer;

import javax.swing.*;
import java.applet.Applet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.*;

public class AE_Training_Graphs extends Applet implements Runnable {
    private static BackPropMultiHiddenLayer     network;
    private static double                       test_set[][];
    private static double                       training_set[][];
    private static double                       bad_inputs[][];
    private final static int                    screen_size             =   500;
    private final static int                    test_set_size           =   14;
    private final static double                 training_set_portion    =   .8;
    private final static int                    training_set_size       =   (int)(training_set_portion * test_set_size);
    private final static int                    graph_axis_size         =   20;
    private final static int                    output_length           =   (int)Math.pow(graph_axis_size,2);
    private final static int                    input_length            =   (int)Math.pow(graph_axis_size,2);
    private final static double                 eta                     =   0.1;
    private final static double                 alpha                   =   0.01;
    private final static double                 accepted_mse            =   1.0;
    private final static int                    max_iterations          =   100000;
    private final static DecimalFormat          df                      =   new DecimalFormat("####.####");
    private static enum                         Set                         {TEST,TRAIN,BAD}
    private static ArrayList<double[]>          error                   =   new ArrayList<double[]>();
    private static Graphics                     g                       =   null;
    private Thread                              t                       =   null;
    private volatile boolean                    running                 =   false;
    private static Runnable                     painter                 =   new Runnable(){
            public void run(){
                Color colors[] = {Color.green, Color.blue, Color.red};
                g.setColor(Color.white);
                g.fillRect(0,0, screen_size, screen_size);
                int y1[]=new int[3], y2[]=new int[3];
                int i= error.size()-(error.size()% screen_size);
                for (int pos=0; i< error.size(); pos++, i++, y1=y2){
                    for (int j=0;j<y1.length; j++)
                        y2[j]=(int)(screen_size - error.get(i)[j]);
                    for (int j=0; j<y1.length; j++){
                        g.setColor(colors[j]);
                        g.drawLine(pos-1<0?0:pos-1,y1[j],pos,y2[j]);
                    }
                }
            }
        };


    public void init() {
         try {
            initTrainingSet();
            g =this.getGraphics();
            g.setColor(Color.white);
            g.fillRect(0,0, screen_size, screen_size);
            network = new BackPropMultiHiddenLayer(new int[]{input_length, 5,2,5, output_length});
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
        check_classification(AE_Training_Graphs.Set.TRAIN, false);
        check_classification(AE_Training_Graphs.Set.TEST, false);
        check_classification(AE_Training_Graphs.Set.BAD, false);
    }

    private static void initTrainingSet(){
        test_set = new double[test_set_size][input_length];
        training_set = new double[training_set_size][input_length];
        for (int i=0; i< test_set_size; i++){
            double k[]= generate_inputPatterns((int)(Math.random()*2)*(Math.random()>.5?1:-1),(int)(Math.random()*4)*(Math.random()>.5?1:-1));
            test_set[i] = k;
        }
        for (int i=0; i< training_set_size; i++){
            training_set[i] = test_set[(int)(Math.random()* test_set_size)];
        }
        generate_bad_inputs();
    }
    private static void train(){
        try{
            int i, j, k; double e, e1, e2;
            for (i=0, e= accepted_mse; (i< max_iterations && e>= accepted_mse); i++, SwingUtilities.invokeAndWait(painter)) {
                System.out.println("Epoch: "+i+"\t\tTraining error: "+ df.format(e)+"\t\tTest Error: "+ df.format(e1= get_test_set_error())+"\t\tBad Input Error: "+ df.format(e2= get_bad_input_error()));
                error.add(new double[]{e,e1,e2});
                for (j=0, k=0, e=0; j< training_set_size; j++, k=(int)(Math.random()* training_set_size)){ /*k for online learning option */
                    e += network.train(training_set[j], training_set[j], eta, alpha);
                }
            }
        } catch(Exception ex){
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }
    private static void check_classification(AE_Training_Graphs.Set set, boolean print_patterns){
        int set_size=0;
        String set_name="";
        double[][] data_set=new double[0][0];
        switch(set){
            case TRAIN: set_size= training_set_size; set_name="Training Set"; data_set= training_set; break;
            case TEST: set_size= test_set_size; set_name="Test Set"; data_set= test_set; break;
            case BAD: set_size=10; set_name="Bad Input Set"; data_set= bad_inputs; break;
        }
        int success_rate=0;
        try{
            for (int i=0; i<set_size; i++) {
                if (print_patterns){
                    for (int j=0; j< input_length; j++)
                        System.out.print((int) data_set[i][j]+" ");
                    System.out.print("\t-->\t");
                }
                double output[]= network.run(data_set[i]);
                double k[] = new double[input_length];
                for(int j=0; j< input_length; j++){
                    k[j]=output[j]>0.5?1:0;
                    if (print_patterns) System.out.print((output[j]>0.5?1:0)+" ");
                }
                if (print_patterns) System.out.print("\n");
                if (Arrays.equals(k, data_set[i]))
                    success_rate++;
            }
            System.out.println("Reproduced "+success_rate+" patterns out of "+ set_size+" of "+set_name+".");
        } catch(Exception ex){
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }
    private static double[] generate_inputPatterns(int m, int b){
        int h = graph_axis_size /2;
        double pattern[] = new double[input_length];
        for (int i=0; i< input_length; i++)
            pattern[i]=0;
        for (int i=0, x=-(h-1); i< graph_axis_size; i++, x++){
            int y=m*x+b;
            int real = h-y;
            int pos = ((real)* graph_axis_size)+i;
            if (pos < input_length && pos >= 0)
                pattern[pos] = 1;
        }
        return pattern;
    }
    private static double get_test_set_error(){
        double e=0;
        try{
            for (int i=0; i< test_set_size; i++){
                double k[] = network.run(test_set[i]);
                for (int j=0; j<k.length; j++)
                    e += .5*Math.pow(test_set[i][j]-k[j], 2);
            }
        } catch (Exception ex){
            System.out.println(ex.getMessage());
            System.exit(5);
        }
        return e;
    }
    private static void generate_bad_inputs(){
        int slopes[] = {-3,3,-4,4,-5,5};
        int intercepts[] = {-5,5,-6,6,-7,7};
        bad_inputs = new double[10][input_length];
        for(int i=0; i<10; i++){
            bad_inputs[i] = generate_inputPatterns(slopes[(int)(Math.random()*6)], intercepts[(int)(Math.random()*6)]);
        }
    }
    private static double get_bad_input_error(){
        double e=0;
        try{
            for (int i=0; i<10; i++){
                double k[] = network.run(bad_inputs[i]);
                for (int j=0; j<k.length; j++)
                    e += .5*Math.pow(bad_inputs[i][j]-k[j], 2);
            }
        } catch (Exception ex){
            System.out.println(ex.getMessage());
            System.exit(6);
        }
        return e;
    }
}
