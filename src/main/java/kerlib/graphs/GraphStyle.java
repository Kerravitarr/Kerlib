/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

import java.awt.Graphics2D;

/**
 *Стиль графика
 * @author Kerravitarr (github.com/Kerravitarr)
 */
public abstract class GraphStyle {
    ///Задать цвет для графика
    public static class Color extends GraphStyle {
        ///Цвет
        private final java.awt.Color color;
        ///Создать цвет
        /// @param color Цвет
        public Color(java.awt.Color c){
            this.color = c;
        }
        @Override
        public java.util.function.Consumer<Graphics2D> add(Graphics2D g) {
            var oc = g.getColor();
            g.setColor(this.color);
            return o -> o.setColor(oc);
        }
    };

    /**Установить стиль
     * @param g
     */
    public void set(Graphics2D g){
        back = add(g);
    }
    /**Снять стиль
     * @param g
     */
    public final void unset(Graphics2D g){
        back.accept(g);
    }
    
    ///Функция, которую должен реализовать наследник.
    /// @param g холст, к которому надо применить стиль
    /// @return функция, которая будет вызывана и которая должна все изменения на холсте отменить
    protected abstract java.util.function.Consumer<Graphics2D> add(Graphics2D g);

    ///Функция, возвращающая всё взад
    private java.util.function.Consumer<Graphics2D> back;
}
