/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.graphs.styles;

import java.awt.Graphics2D;
import kerlib.graphs.GraphStyle;

///Задать цвет для графика
public class Gcolor extends GraphStyle {
    ///Цвет
    private final java.awt.Color color;
    ///Создать цвет
    /// @param color Цвет
    public Gcolor(java.awt.Color c) {
        this.color = c;
    }

    @Override
    public java.util.function.Consumer<Graphics2D> add(Graphics2D g) {
        java.awt.Color oc = g.getColor();
        g.setColor(this.color);
        return o -> o.setColor(oc);
    }
    
}
