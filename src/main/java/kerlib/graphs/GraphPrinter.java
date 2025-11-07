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
    ///@return текущий объект
    public GraphPrinter setPP(PointPrinter pp) {
        this.pointPrinter = pp;
        return this;
    }
    ///@param trend объект, который отрисует линию тренда
    public GraphPrinter setTrendLine(GraphPrinter trend){
        this.trendLine = trend;
        return this;
    }

    ///Отрисовывает на холсте все точки
    /// @param g Графический контекст
    /// @param points Список точек
    /// @param x Ось x
    /// @param y Ось y
    final void draw(Graphics2D g, List<Point2D> points, Axis<?> x, Axis<?> y){
        var localPoints = points.stream().map(p -> (Point2D) new Point2D.Double(x.x(p), y.y(p))).toList();
        //Линия тренда первая, потому что она должна скрыться под график
        if(trendLine != null)trendLine.draw(g, localPoints);
        draw(g, localPoints);
    }
    ///Отрисовывает на холте точки
    /// @param g Графический контекст
    /// @param list Список точек уже в координатах экрана
    protected abstract void draw(Graphics2D g, List<Point2D> list);

    ///Нарисовать точку графика в заддном месте
    /// @param g Графический контекст
    /// @param x Координата x
    /// @param y Координата y
    protected void drawMarker(Graphics2D g, double x, double y) {
        if(pointPrinter != null)
            pointPrinter.point(g, x, y);
    }
    ///Нарисовать точку графика в заддном месте
    /// @param g Графический контекст
    /// @param p Точка
    protected void drawMarker(Graphics2D g, Point2D p) {
        drawMarker(g, p.getX(), p.getY());
    }
    ///Объект, который будет печаать точки графика. Сами маркеры точек
    private PointPrinter pointPrinter;
    ///Объект для печати линии тренда, если есть
    private GraphPrinter trendLine;
}
