package mlp;

/**
 * Author: asalem
 * Description: mlp.Nueral Network Implementation for Exclusive OR Function
 * Date: Feb 10, 2007
 */

public class Nueral {
    public static void main(String[] args){
        //This is just a silly way to send through the boolean stimulus <0,0> <0,1> <1,0> <1,1>
        for (int i=1; i<=4; i++)
            System.out.println(i%2 + " | " + (int)(i%1.5) + " | " + sendStimulus(i%2, (int)(i%1.5)));
    }
    /*---- Layer 1 ----*/
    private static boolean sendStimulus(int s1, int s2){
        return r(h(s1,s2,-1/2,false), h(s1,s2,-3/2,true));
    }
    /*---- Layer 2 ----*/
    private static int h(int s1, int s2, int bias, boolean inhibitor){
       return (s1+s2+bias > 0) ? (inhibitor ? -1 : 1) : 0;
    }
    /*---- Layer 3 ----*/
    private static boolean r(int h1, int h2){
        return (h1+h2==1);
    }
}