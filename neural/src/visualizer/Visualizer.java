package visualizer;

import java.text.DecimalFormat;
import java.applet.Applet;
import java.io.IOException;
import java.awt.*;
import java.awt.event.*;


public class Visualizer extends Applet implements Runnable, MouseListener, MouseMotionListener {
             Objects                    objects =   new Objects();
    volatile boolean                    stop;
    volatile BackPropMultiHiddenLayer   network;
             VisualizerTrainer          vt;
             Thread                     t;

    //-------------  Region Applet Life Cycle --------------//
    public void init(){
        try{
            readConfig();
            addMouseListener(this);
            addMouseMotionListener(this);
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
    public void run(){
        while (!stop)
            if (Thread.currentThread() ==  t)
                if (!objects.waitFlag)
                    repaint();
                else
                    askContinue(objects.waitReasonError);
        repaint();
    }
    public synchronized void stop(){
        stop=true;
        t=null;
        vt=null;
        objects=null;
        network=null;
    }
    public synchronized void askContinue(boolean causedByError){
        objects.waitFlag = false;
        //vt.resume();
    }
	public void mouseClicked(MouseEvent e){
        if ((e.getX() > objects.screen_size-50) && (e.getY() > objects.screen_size)){
            if (stop){
                stop=false;
                if (objects.epoch>=objects.max_iterations)
                    objects.bypass_iterations=true;
                if (objects.error>=objects.accepted_mse)
                    objects.bypass_error=true;
                vt = new VisualizerTrainer(this, objects);
                vt.start();
                t = new Thread(this);
                t.start();
            } else {
                stop=true;
            }
        }
	}
    public void mousePressed(MouseEvent e){ }
    public void mouseReleased(MouseEvent e){ }
    public void mouseEntered(MouseEvent e){ }
    public void mouseExited(MouseEvent e){ }
    public void mouseDragged(MouseEvent e){ }
    public void mouseMoved(MouseEvent e){
        if ((e.getX() > objects.screen_size-50) && (e.getY() > objects.screen_size)){
            this.setCursor(new Cursor(Cursor.HAND_CURSOR));
        }else{
            this.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }
    }
    
    //-------------  Region Painting Functions ------------//
    public void readConfig() throws Exception {
        try{
            if (this.getHeight() != this.getWidth())
                throw new Exception("Applet Dimensions must be square!");
            this.setFont(new Font("TimesRoman", Font.BOLD, 12));
            objects.font_size = this.getFontMetrics(this.getFont()).getHeight();
            objects.true_size = this.getHeight();
            objects.screen_size = objects.true_size-objects.font_size;
            objects.offset_left = objects.font_size/2;
            objects.offset_right = objects.true_size-objects.offset_left;
            objects.output_string_offset = objects.offset_left + (this.getFontMetrics(this.getFont()).charWidth(' ')*50); 
            objects.eta = Double.parseDouble(getParameter("eta").trim());
            objects.alpha = Double.parseDouble(getParameter("alpha").trim());
            objects.accepted_mse = Double.parseDouble(getParameter("mse").trim());
            objects.confidence_interval = Double.parseDouble(getParameter("confidence").trim());
            objects.max_iterations = Integer.parseInt(getParameter("epochs").trim());
            objects.classify_layer = Integer.parseInt(getParameter("classify").trim());
            objects.visualize_layer = Integer.parseInt(getParameter("visualize").trim());
            objects.circle_size = Integer.parseInt(getParameter("circle_size").trim());
            objects.learning_mode = getParameter("learning_mode").equals("Batch") ? Objects.Learning_Mode.BATCH : Objects.Learning_Mode.ONLINE;

            objects.network_size = parseArray(getParameter("network_size"), 0);
            objects.training_set = parse_dataset(getParameter("set1"));
            objects.target_set = parse_dataset(getParameter("target1"));
            objects.test_set = parse_dataset(getParameter("set2"));
            objects.training_representation = new double[objects.training_set.length][2];
            objects.test_representation = new double[objects.test_set.length][2];
            objects.train_colors = getColors(getParameter("color1"), 1);
            objects.test_colors = getColors(getParameter("color2"), 2);
            objects.classify_colors = getColors(getParameter("color3"), 3);
            
        }catch(Exception ex){
            //Recover state.
            throw new Exception("Error While Reading Config: "+ex.getMessage());
        }
        
    }
    public int[][] getColors(String string, int code) throws Exception {
        int x = code==1 ? objects.training_set.length : code==2 ? objects.test_set.length : objects.network_size[objects.classify_layer+1];
        int a[][] = new int[x][4];
        try{
            if (string.equals("empty")){
                for (int i=0; i<x; i++) {
                    for (int j=0; j<4; j++)
                        a[i][j]=(j==0)?254:0;
                }
                return a;
            }
            String c[] = string.trim().split(",");
            for (int i=0; i<c.length; i++){
                for (int j=0; j<4; j++){
                    int aw = j==0 ? Math.abs(9-(Integer.parseInt(""+c[i].trim().charAt(j)))) : Integer.parseInt(""+c[i].trim().charAt(j));
                    a[i][j] = (int)(aw*28.33);
                }
            }
        }catch(Exception e){
            throw new Exception("Error when parsing colors: "+e.getMessage());
        }
        return a;
    }
    public int[] parseArray(String string, int code) throws Exception {
        try{
            String a[] = string.trim().split(",");
            int array[] = new int[a.length];
            for (int i=0; i<a.length; i++)
                array[i] = Integer.parseInt(a[i].trim());
            return array;
        } catch(Exception e){
            throw new Exception("Error while parsing array: "+e.getMessage());
        }
    }
    public double[][] parse_dataset(String string) throws Exception{
        try{
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
        } catch(Exception e){
            throw new Exception("Error while parsing dataset: "+e.getMessage());
        }
    }
    public int[][] find_classification_lines(double[] variables, double confidence){
        int point[][] = new int[3][4];
        int y;
        for (int x=0; x<3; x++){
            for (int i=0; i<objects.network_size[objects.network_size.length-1]; i++){
                double w1=variables[0];
                double w2=variables[1];
                double b=(x==0)?variables[2]:(x==1)?variables[2]+confidence:variables[2]-confidence;
                if (w2 != 0){
                    point[x][0]=0;
                    point[x][1]=(int)((((-(0*w1))-b)/w2)*objects.screen_size);
                    point[x][2]=objects.screen_size;
                    point[x][3]=(int)((((-(1*w1))-b)/w2)*objects.screen_size);
                } else { //Its a verticle line
                    point[x][0]=(y=(int)((((-(0*w2))-b)/(w1==0/*Not likely*/?0.000001:w1))*objects.screen_size));
                    point[x][1]=0;
                    point[x][2]=y;
                    point[x][3]=objects.screen_size;
                }
            }
        }
        return point;
    }
    public void paint(Graphics g){
        objects.paint_count++;
        int size=objects.circle_size;
        g.setColor(Color.white);
        g.fillRect(objects.offset_left,0, objects.screen_size, objects.screen_size);
        g.setColor(Color.black);
        g.drawRect(objects.offset_left,0, objects.screen_size, objects.screen_size);
        for (int i=0; i<objects.training_set.length; i++){
            g.setColor(new Color(objects.train_colors[i][1],objects.train_colors[i][2],objects.train_colors[i][3],objects.train_colors[i][0]));
            double point[] = objects.training_representation[i];
            g.fillOval((int)point[0]-(size/2)+objects.offset_left, (int)point[1]-(size/2), size, size);

        }
        for (int i=0; i<objects.test_set.length; i++){
            g.setColor(new Color(objects.test_colors[i][1],objects.test_colors[i][2],objects.test_colors[i][3],objects.test_colors[i][0]));
            double point[] = objects.test_representation[i];
            g.fillOval((int)point[0]-(size/2)+objects.offset_left, (int)point[1]-(size/2), size, size);

        }
        for (int i=0; i<objects.classifier_variables.length; i++){
            g.setColor(new Color(objects.classify_colors[i][1],objects.classify_colors[i][2],objects.classify_colors[i][3],objects.classify_colors[i][0]));
            int line[][] = find_classification_lines(objects.classifier_variables[i], objects.confidence_interval);
            for (int j=0; j<3; j++){
                g.drawLine(line[j][0]+objects.offset_left, line[j][1], line[j][2]+objects.offset_left, line[j][3]);
            }
        }
        g.setColor(Color.GRAY);
        g.fillRect(objects.offset_left, objects.screen_size, objects.screen_size,  objects.font_size);
        g.setColor(Color.BLACK);
        g.drawRect(objects.offset_left, objects.screen_size, objects.screen_size,  objects.font_size);
        g.drawString("Network Error: "+ objects.df.format(objects.error), objects.offset_left+5, objects.true_size-2);
        g.drawString("Epoch: "+objects.epoch, objects.output_string_offset, objects.true_size-2);
        //Remove math here and precalculate this
        g.fillRect(objects.screen_size+objects.offset_left-60, objects.screen_size, 60, objects.true_size-objects.screen_size);
        g.setColor(Color.WHITE);
        g.drawString(stop?"Resume":"Pause", objects.screen_size+objects.offset_left-60, objects.true_size-2);
    }
    public void update(String s){
        System.out.println(s);
    }
}

class VisualizerTrainer extends Thread {
    Objects                     objects;
    Visualizer                  visualizer;


    VisualizerTrainer(Visualizer visualizer, Objects objects){
        super("VisualizerTrainer");
        this.objects = objects;
        this.visualizer = visualizer;
    }
    public void moveMemory(){
        try{
            objects.classifier_variables = visualizer.network.getClassifierVariables(objects.classify_layer);
            for (int x=0; x<objects.training_set.length; x++){
                visualizer.network.run(objects.training_set[x]);
                objects.training_representation[x] = visualizer.network.getLayer(objects.visualize_layer, objects.screen_size);
            }
            for (int x=0; x<objects.test_set.length; x++){
                visualizer.network.run(objects.test_set[x]);
                objects.test_representation[x] = visualizer.network.getLayer(objects.visualize_layer, objects.screen_size);
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
                for (objects.epoch=0, objects.error=objects.accepted_mse; ((objects.epoch<objects.max_iterations || objects.bypass_iterations) && (objects.error>=objects.accepted_mse || objects.bypass_error) && (!(visualizer.stop))); objects.epoch++) {
                    for (j=0, k=0, objects.error=0; j<objects.training_set.length; j++, k=(int)(Math.random()*objects.training_set.length)){
                        l = (objects.learning_mode==Objects.Learning_Mode.BATCH) ? j : k;
                        objects.error += visualizer.network.train(objects.training_set[l], objects.target_set[l], objects.eta, objects.alpha);
                    }
                    if ((objects.paint_count > objects.repainter)){
                        objects.repainter=objects.paint_count;
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
    public volatile double  training_set[][];
    public volatile double  target_set[][];
    public volatile double  test_set[][];
    public volatile double  training_representation[][];
    public volatile double  test_representation[][];

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
    public int              screen_size             =   500-16;
    public int              true_size               =   500;
    public int              offset_left             =   8;
    public int              offset_right            =   492;
    public int              output_string_offset    =   180;
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
    public volatile int     repainter               =   0;
    public volatile boolean bypass_error;
    public volatile boolean bypass_iterations;
    public volatile boolean waitFlag;
    public volatile boolean waitReasonError;


    //Display Configuration
    public DecimalFormat    df                      =   new DecimalFormat("####.####");
    public int              circle_size             =   10;
    public float            colors[][]              =   new float[set_size][4];
    public int              font_size               =   10;
    public int              train_colors[][]        =   new int[0][0];
    public int              test_colors[][]         =   new int[0][0];
    public int              classify_colors[][]     =   new int[0][0];
    public double           confidence_interval     =   .1;
}