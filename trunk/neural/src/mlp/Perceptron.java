package mlp; /**
 * Author: asalem
 * Description: mlp.Perceptron Learning Implementation for AND Function
 * Date: Feb 10, 2007
 */

import java.util.Arrays;

public class Perceptron {
    final static double  ETA            =   0.1;
    final static double  bias           =   -1;
    final static int     MAXITERATIONS  =   100;
    final static Input   n[]            =   {new Input(0,0), new Input(0,1), new Input(1,0), new Input(1,1)};
    static int           t[]            =   {0,0,0,1};
    static int           r[]            =   new int[4];
    static double        w[]            =   new double[2];

    public static void main(String[] args){
        int i;
        for (i=0; i<w.length; i++)
            w[i] = Math.random()*(Math.random() > .5 ? 1 : -1);
        for (i=0; ((i<MAXITERATIONS) && !Arrays.equals(r,t)); i++, processResponse())
            train((int)(Math.random()*4));
        System.out.println("Solution Found: "+Arrays.equals(r,t)+".\tNumber of iterations: "+i+".\tBias: "+ bias +".");
        for (i=0;i<w.length;i++) System.out.println("Weight " + i + ": " + w[i]);
    }
    private static void train(int selection){
        for (int i=0; i<w.length; i++)
            w[i] += (t[selection]-r[selection])*ETA*n[selection].s[i];
    }
    private static void processResponse(){
        for (int i=0; i<n.length; i++)
            r[i] = ((n[i].s[0]*w[0])+(n[i].s[1]*w[1])+bias < 0) ? 0 : 1;
    }
    private static class Input{
        protected int s[] = new int[2];
        public Input(int s1, int s2){ this.s[0]=s1; this.s[1]=s2; }
    }
}