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

    ///Отрисовывает на холстве все точки
    protected abstract void draw(Graphics2D g, List<Point2D> points, Axis<?> x, Axis<?> y);

}
