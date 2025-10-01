/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

/**Пунктирная линия*/
public class Bash {

    float[] dash;
    float dash_phase = 0;

    public Bash(int lenght) {
        this(lenght, lenght);
    }

    public Bash(int lenghtHatch, int pause) {
        if (lenghtHatch == 0 || pause == 0) {
            dash = new float[]{Float.MAX_VALUE};
        } else {
            dash = new float[]{lenghtHatch, pause};
        }
    }

}
