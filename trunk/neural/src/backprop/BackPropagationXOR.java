package backprop;

import backprop.BackPropagation;

import java.awt.*;

/**
 * Author: asalem
 * Description:
 * Date: Feb 20, 2007
 */

public class BackPropagationXOR {
    public static void main(String args[]){
        try{
            BackPropagation network = new BackPropagation(2,2,1);
            Point points[] = {new Point(0,0), new Point(0,1), new Point(1,0), new Point(1,1)};
            for (int i=0; i<13600; i++){
                int j = (int)(Math.random()*4);
                double t = ((j==1) || (j==2)) ? 1 : 0;
                network.train(new double[]{points[j].x, points[j].y}, new double[]{t}, 0.5, 0.0);
            }
            for (int i=0; i<4; i++){
                System.out.println(points[i].x+","+points[i].y+" = "+
                    network.run(new double[]{points[i].x, points[i].y})[0]);
            }
        }catch(Exception e){
                System.out.println(e.getMessage());
                System.exit(1);
        }
    }
}
