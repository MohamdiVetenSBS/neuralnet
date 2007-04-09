package backprop;

/**
 * Author: asalem
 * Description:
 * Date: Feb 13, 2007
 */

import java.util.Arrays;


public class AutoEncoder  {
    private static BackPropagation          network;
    private static double                   training_set[][];
    private final static int                training_set_size       =   24;
    private final static int                hidden_units            =   2;
    private final static int                output_length           =   24;
    private final static int                input_length            =   24;
    private final static double             eta                     =   0.1;
    private final static double             alpha                   =   0.01;
    private final static double             accepted_mse            =   4;
    private final static int                max_iterations          =   1000000;

    public static void main(String args[]) {
        initTrainingSet();
        try {
            network = new BackPropagation(input_length,hidden_units,output_length);
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
            for (int j=0; j<input_length; j++){
                training_set[i][j] = (j==i ? 1 : 0);
            }
        }
    }
    private static void train(){
        try{
            int i, j, k; double e;
            for (i=0, e=accepted_mse; (i<max_iterations && e>=accepted_mse); i++, System.out.println("Network error:\t"+e+"  \tEpoch: "+i)) {
                for (j=0, k=0, e=0; j<training_set_size; j++, k=(int)(Math.random()*training_set_size)){ /*k for online learning option */
                    e += network.train(training_set[j],training_set[j], eta, alpha);
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
            for (int i=0; i<training_set_size; i++) {
                for (int j=0; j<input_length; j++)
                    System.out.print((int)training_set[i][j]+" ");
                System.out.print("\t-->\t");
                double output[]=network.run(training_set[i]);
                int success=0;
                double k[] = new double[input_length];
                for(int j=0; j<input_length; j++){
                    k[j]=output[j]>0.5?1:0;
                    System.out.print((output[j]>0.5?1:0)+" ");
                }
                System.out.print("\n");
                if (Arrays.equals(k,training_set[i]))
                    success_rate++;
            }
            System.out.print("Reproduced "+success_rate+" patterns out of "+training_set_size);
        } catch(Exception ex){
            System.out.println(ex.getMessage());
            System.exit(1);
        }
    }

    private static void print_weights(){
        for (int i=0; i<network.output_size; i++){
            
        }
    }
}
