package visualizer;

import java.text.DecimalFormat;
import java.applet.Applet;
import java.io.IOException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;


public class Visualizer extends Applet implements Runnable, ActionListener {
             Objects                    objects =   new Objects();
    volatile boolean                    stop;
    volatile BackPropMultiHiddenLayer   network;
             VisualizerTrainer          vt;
             Thread                     t;

    public void init(){
        try{
            readConfig();
//            System.out.println(objects.screen_size);
//            System.out.println(objects.eta);
//            System.out.println(objects.alpha);
//            System.out.println(objects.accepted_mse);
//            System.out.println(objects.max_iterations);
//            System.out.println(objects.classify_layer);
//            System.out.println(objects.visualize_layer);
//            for (int i=0; i<objects.network_size.length; i++)
//                System.out.print(objects.network_size[i]+",");
//            System.out.print("\n\n");
//            for (int i=0; i<objects.training_set.length; i++, System.out.print("\n"))
//                for (int j=0; j<objects.training_set[i].length; j++)
//                    System.out.print((int)objects.training_set[i][j]);
//            System.out.print("\n\n");
//            for (int i=0; i<objects.target_set.length; i++, System.out.print("\n"))
//                for (int j=0; j<objects.target_set[i].length; j++)
//                    System.out.print((int)objects.training_set[i][j]);
//            System.exit(1);
            network = new BackPropMultiHiddenLayer(objects.network_size);
        } catch (IOException ioex){

        } catch (Exception ex){

        }

    }
    public synchronized void start(){
        if (t==null){
           if (vt==null){
                vt = new VisualizerTrainer(this, objects);
                vt.start();
            }
            stop = false;
            t = new Thread(this);
            t.start();
        }
    }
    public synchronized void stop(){
        stop=true;
        t=null;
        vt=null;
        objects=null;
        network=null;
    }
    public void run(){
        while (!stop)
            if (Thread.currentThread() ==  t)
                if (!objects.waitFlag)
                    repaint();
                else
                    askContinue(objects.waitReasonError);
        repaint();
    }
    public void readConfig() throws Exception {
        try{
            if (this.getHeight() != this.getWidth())
                throw new Exception("Applet Dimensions must be square!");
            objects.screen_size = this.getHeight();
            objects.eta = Double.parseDouble(getParameter("eta").trim());
            objects.alpha = Double.parseDouble(getParameter("alpha").trim());
            objects.accepted_mse = Double.parseDouble(getParameter("mse").trim());
            objects.max_iterations = Integer.parseInt(getParameter("epochs").trim());
            objects.classify_layer = Integer.parseInt(getParameter("classify").trim());
            objects.visualize_layer = Integer.parseInt(getParameter("visualize").trim());
            objects.circle_size = Integer.parseInt(getParameter("circle_size").trim());
            objects.train_on = Integer.parseInt(getParameter("train_on").trim());
            
            String network_size[] = getParameter("network_size").trim().split(",");
            int net_size[] = new int[network_size.length];
            for (int i=0; i<network_size.length; i++)
                net_size[i] = Integer.parseInt(network_size[i].trim());
            objects.network_size = net_size;
            objects.learning_mode = getParameter("learning_mode").equals("Batch") ? Objects.Learning_Mode.BATCH : Objects.Learning_Mode.ONLINE;

            for (int i=1; i<=3; i++){
                double k[][] = parse_dataset(getParameter("set"+i));
                objects.training.add(k);
                objects.target.add(parse_dataset(getParameter("target"+i)));
                objects.visual.add(new double[k.length][2]);
            }
            objects.training_set = objects.training.get(objects.train_on);
            objects.target_set = objects.target.get(objects.train_on);
        }catch(Exception ex){
            //Recover state.
            throw ex;
        }
        
    }
    public double[][] parse_dataset(String string){
        if (string.equals("empty")){
            return new double[0][0];
        }
        int x,y;
        String data_set1[] = string.split("#");
        String data_set11[][] = new String[x=data_set1.length][y=data_set1[0].trim().split(",").length];
        double dset[][] = new double[x][y];
        for (int i=0; i<x; i++)
            data_set11[i] = data_set1[i].trim().split(",");
        for (int i=0; i<x; i++)
            for (int j=0; j<y; j++)
                dset[i][j] = Double.parseDouble(data_set11[i][j]);
        return dset;
    }
    public double[] find_classification_lines(double[] variables){
        double point[] = new double[4];
        int half = (objects.screen_size/2);
        for (int i=0; i<objects.network_size[objects.network_size.length-1]; i++){
            double w1=variables[0];
            double w2=variables[1];
            double b=variables[2];
            point[0]=0;
            point[1]=(((-(0*w1))-b)/w2)*objects.screen_size;
            point[2]=objects.screen_size;
            point[3]=(((-(1*w1))-b)/w2)*objects.screen_size;
        }
        return point;
    }
    public void paint(Graphics g){
        objects.paint_count++;
        int size=objects.circle_size;
        g.setColor(Color.white);
        g.fillRect(0,0, objects.screen_size, objects.screen_size);
        for (int x=0; x<3; x++){
            g.setColor(objects.colors[x]);
            for (int i=0; i<objects.visual.get(x).length; i++){
                double point[] = objects.visual.get(x)[i];
                g.fillOval((int)point[0]-(size/2), (int)point[1]-(size/2), size, size);
            }
        }
        g.setColor(Color.black);
        for (int i=0; i<objects.classifier_variables.length; i++){
            double line[] = find_classification_lines(objects.classifier_variables[i]);
            g.drawLine((int)line[0], (int)line[1], (int)line[2], (int)line[3]);
        }
    }
    public void update(String s){
        System.out.println(s);
    }
    public synchronized void askContinue(boolean causedByError){
        objects.waitFlag = false;
        //vt.resume();
    }
    public synchronized void actionPerformed(ActionEvent event) {

    }
    public double getError(){
        return objects.error;
    }
    public double getEpoch(){
        return objects.epoch;
    }
    public boolean getStop(){
        return stop;
    }
}

class VisualizerTrainer extends Thread {
    Objects                     objects;
    Visualizer                  visualizer;
    int                         repainter=0;
    VisualizerTrainer(Visualizer visualizer, Objects objects){
        super("VisualizerTrainer");
        this.objects = objects;
        this.visualizer = visualizer;
    }
    public void moveMemory(){
        try{
            objects.classifier_variables = visualizer.network.getClassifierVariables(objects.classify_layer);
            for (int x=0; x<3; x++){
                for (int y=0; y<objects.training.get(x).length; y++){
                    visualizer.network.run(objects.training.get(x)[y]);
                    objects.visual.get(x)[y] = visualizer.network.getLayer(objects.visualize_layer, objects.screen_size);
                }
            }
        } catch (Exception ex){
            System.out.println(ex.getMessage());
            System.exit(205);
            visualizer.stop = true;
        }
    }

    public void run(){
        int j,k,l;
//        while(true){
            try{
                for (objects.epoch=0, objects.error=objects.accepted_mse; ((objects.epoch<objects.max_iterations || objects.bypass_iterations) && (objects.error>= objects.accepted_mse || objects.bypass_error)); objects.epoch++, visualizer.update("Network error:\t"+objects.df.format(objects.error)+"  \tEpoch: "+objects.epoch)) {
                    for (j=0, k=0, objects.error=0; j<objects.training_set.length; j++, k=(int)(Math.random()*objects.training_set.length)){
                        l = (objects.learning_mode==Objects.Learning_Mode.BATCH) ? j : k;
                        objects.error += visualizer.network.train(objects.training_set[l], objects.target_set[l], objects.eta, objects.alpha);
                     }
                    if ((objects.paint_count > this.repainter)){
                        this.repainter=objects.paint_count;
                        moveMemory();
                    }
                }

//                synchronized(this){
//                    objects.waitFlag=true;
//                    objects.waitReasonError = objects.error<objects.accepted_mse && !objects.bypass_error;
//                    //wait();
//                }
                moveMemory();
                visualizer.stop = true;
            } catch (Exception ex){
                System.out.println(ex.getMessage());
                System.exit(200);
                visualizer.stop = true;
            }
//        }
    }
}

class Objects {
    //DataSets
    public volatile ArrayList<double[][]> training = new ArrayList<double[][]>();
    public volatile ArrayList<double[][]> target = new ArrayList<double[][]>();
    public volatile ArrayList<double[][]> visual = new ArrayList<double[][]>();

    public volatile int     train_on = 0;
    public volatile double  training_set[][];
    public volatile double  target_set[][];

//                                                = {{1,0,0,0,0,0,0,0,0,0},  Add Default config?
//                                                 {0,1,0,0,0,0,0,0,0,0},
//                                                 {0,0,1,0,0,0,0,0,0,0},
//                                                 {0,0,0,1,0,0,0,0,0,0},
//                                                 {0,0,0,0,1,0,0,0,0,0},
//                                                 {0,0,0,0,0,1,0,0,0,0},
//                                                 {0,0,0,0,0,0,1,0,0,0},
//                                                 {0,0,0,0,0,0,0,1,0,0},
//                                                 {0,0,0,0,0,0,0,0,1,0},
//                                                 {0,0,0,0,0,0,0,0,0,1}}

    //Network and Set Objects
    public int[]            network_size            =   {10,2,10};
    public int              visualize_layer         =   0;  /* O-based Note:Input is not a Layer!*/
    public int              classify_layer          =   1;
    public int              screen_size             =   500;
    public int              set_size                =   10;
    public enum             Learning_Mode               {ONLINE, BATCH}
    public Learning_Mode    learning_mode           =   Learning_Mode.BATCH;

    //Training Configuration & Variables
    public double           eta                     =   0.1;
    public double           alpha                   =   0.01;
    public double           accepted_mse            =   1.0;
    public int              max_iterations          =   100000;
    public volatile double  visualized_layers[][]   =   new double[set_size][2];
    public volatile double  classifier_variables[][]=   new double[network_size[classify_layer]][4];
    public volatile double  error;
    public volatile int     epoch                   =   0;
    public volatile int     paint_count             =   0;
    public volatile boolean bypass_error;
    public volatile boolean bypass_iterations;
    public volatile boolean waitFlag;
    public volatile boolean waitReasonError;


    //Display Configuration
    public DecimalFormat    df                      =   new DecimalFormat("####.####");
    public int              circle_size             =   10;
    public Color            colors[]                =   {Color.black, Color.blue, Color.red};
}