package backprop;

import java.util.Arrays;

public class AE_MHL_Nonlinear_Reduction {
    private static BackPropMultiHiddenLayer     network;
    private static double                       training_set[][];
    private final static int                    graph_axis_size         =   10;
    private final static int                    training_set_size       =   15;
    private final static int                    output_length           =   100;
    private final static int                    input_length            =   100;
    private final static double                 eta                     =   0.1;
    private final static double                 alpha                   =   0.01;
    private final static double                 accepted_mse            =   3;
    private final static int                    max_iterations          =   1000000;

    public static void main(String args[]) {
        initTrainingSet();
        try {
            network = new BackPropMultiHiddenLayer(new int[]{input_length, 5,2,5, output_length});
            train();
        }catch(Exception e){
            System.out.println(e.getMessage());
            System.exit(3);
        }
        run();

    }

    private static void initTrainingSet(){
        training_set = new double[training_set_size][input_length];
        for (int i=0; i<training_set_size; i++){
            double k[]=generate_inputPatterns((int)(Math.random()*2)*(Math.random()>.5?1:-1),(int)(Math.random()*4)*(Math.random()>.5?1:-1));
            training_set[i] = k;
        }
    }
    private static void train(){
        try{
            int i, j, k; double e;
            for (i=0, e= accepted_mse; (i< max_iterations && e>= accepted_mse); i++, System.out.println("Network error:\t"+e+"  \tEpoch: "+i)) {
                for (j=0, k=0, e=0; j< training_set_size; j++, k=(int)(Math.random()* training_set_size)){ /*k for online learning option */
                    e += network.train(training_set[j], training_set[j], eta, alpha);
                }
            }
        } catch(Exception ex){
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }
    private static void run(){
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

    private static double[] generate_inputPatterns(int m, int b){
        int h = graph_axis_size/2;
        double pattern[] = new double[input_length];
        for (int i=0; i<input_length; i++)
            pattern[i]=0;
        for (int i=0, x=-(h-1); i<graph_axis_size; i++, x++){
            int y=m*x+b;
            int real = h-y;
            int pos = ((real)*10)+i;
            if (pos < input_length && pos >= 0)
                pattern[pos] = 1;
        }
        return pattern;
    }
}
