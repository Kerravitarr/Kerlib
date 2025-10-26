/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;

/**
 *Класс для отрисовки графиков
 * @author Kerravitarr (github.com/Kerravitarr)
 */
public abstract class GraphPrinter {
    ///Функция отрисовки точки на графике
    public static interface PointPrinter {
        ///Функция отрисовки точки на графике
        /// @param g Графический контекст
        /// @param x Координата x
        /// @param y Координата y
        public void point(Graphics2D g,double x, double y);        
    }
    ///@param pp Функция отрисовки точки на графике
    public void setPP(PointPrinter pp) {
        this.pointPrinter = pp;
    }

    ///Отрисовывает на холстве все точки
    protected abstract void draw(Graphics2D g, List<Point2D> points, Axis<?> x, Axis<?> y);

    ///Нарисовать точку графика в заддном месте
    /// @param g Графический контекст
    /// @param x Координата x
    /// @param y Координата y
    protected void drawMarker(Graphics2D g, double x, double y) {
        if(pointPrinter != null)
            pointPrinter.point(g, x, y);
    }

    private PointPrinter pointPrinter;
}
