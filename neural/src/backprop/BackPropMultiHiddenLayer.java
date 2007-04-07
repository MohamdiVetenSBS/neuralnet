package backprop;

import java.util.ArrayList;

/**
 * Author: asalem
 * Description:
 * Date: Feb 13, 2007
 */

public class BackPropMultiHiddenLayer {
    protected layer[]         layers;
	protected int             input_size;
	protected int             output_size;
    private   double          output[];
    private   double          sgm(final double x)   { return (1/(1+Math.exp(-x)));}

    public BackPropMultiHiddenLayer(final int size[]) throws Exception{
        if (size.length < 3) throw new Exception("Bad network parameters!");
        for (int i: size)
            if (i<=0) throw new Exception("Bad network parameters!");
        this.layers = new layer[size.length-1];
        for (int i=0, j=1; j<size.length; i++, j++){
            this.layers[i] = new layer(size[j], size[i]);
        }
        this.input_size = size[0];
        this.output_size = size[size.length-1];
        this.output = new double[size[size.length-1]];
    }

    public double train(final double input[], final double desired[], final double eta, final double alpha) throws Exception {
        if (input.length!=input_size || desired.length!=output_size){
            throw new Exception("Bad training parameters!");
        }
        ArrayList<double[]> deltas = new ArrayList<double[]>();
        for (layer l:layers){
            deltas.add(new double[l.size]);
        }
        double error, sum, mse;
        int i, j;
        //Run Input
        try { output = run(input); }catch(Exception e){ throw e; }
        //Calculate Output Deltas
        for (i=0, mse=0; i<output_size; i++){
            deltas.get(layers.length-1)[i] = (error=(desired[i]-output[i]))*(output[i])*(1-output[i]);
            mse += Math.pow(error,2);
        }
        //Calculate the Hidden Deltas
        for (int x=layers.length-2; x>=0; x--){
            for (i=0; i<layers[x].size; i++) {
                for (j=0, sum=0; j<layers[x+1].size; j++){
                    sum += deltas.get(x+1)[j]*(layers[x+1].units[j].weights[i]);
                }
                deltas.get(x)[i] = sum*(layers[x].units[i].output)*(1-(layers[x].units[i].output));
            }
        }

        //Adjust Weights
        for (int x=layers.length-1; x>=0; x--){
            for (i=0; i<layers[x].size; i++){
                layers[x].units[i].bdelta = (alpha * layers[x].units[i].bdelta) + (eta * (deltas.get(x))[i]);
                layers[x].units[i].bias += layers[x].units[i].bdelta;
                for (j=0; j<((x-1>=0)?layers[x-1].size:input.length); j++){
                    layers[x].units[i].delta[j]  = (alpha * (layers[x].units[i].delta[j])) + (eta * (deltas.get(x))[i] * (x-1>=0?layers[x-1].units[j].output:input[j]));
                    layers[x].units[i].weights[j] += layers[x].units[i].delta[j];
                }
            }
        }
        return .5*mse;
    }
    public double[] run (final double input[]) throws Exception{
        if (input.length!=input_size)
            throw new Exception("Bad run parameters!");
        int i, j; double sum, result[] = new double[output_size];
        for (int x=0; x<layers.length; x++){
            for (i=0; i<layers[x].size; i++) {
                for (j=0, sum=layers[x].units[i].bias; j<(x==0?input_size:layers[x-1].size); j++){
                    sum += ((layers[x].units[i].weights[j]) * (x==0?input[j]:layers[x-1].units[j].output));
                }
                layers[x].units[i].output = sgm(sum);
                if (x==layers.length-1)
                    result[i] = sgm(sum);
            }
        }
        return result;
    }
    protected class layer {
        protected int size, input_size;
        protected neuron units[];
        public layer(final int size, final int input_size){
            this.size = size;
            this.input_size = input_size;
            units = new neuron[size];
            for (int i=0; i<size; i++){
                units[i] = new neuron(input_size);
            }
        }
        protected class neuron{
            double weights[], delta[], output, bias, bdelta;
            public neuron(final int input_size){
                bias = Math.random() * (Math.random() > .5 ? 1 : -1);
                weights = new double[input_size];
                delta = new double[input_size];
                for (int i=0; i<input_size; i++){
                    weights[i] = Math.random() * (Math.random() > .5 ? 1 : -1);
                    delta[i] = 0;
                }
            }
        }
    }
}
