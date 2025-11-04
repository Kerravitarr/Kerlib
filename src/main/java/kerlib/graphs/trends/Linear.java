/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs.trends;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;
import kerlib.graphs.GraphPrinter;

/** Линия линейного тренда
 * @author Kerravitarr (github.com/Kerravitarr)
 */
public class Linear extends GraphPrinter {
    public Linear() {}
    @Override
    public void draw(Graphics2D g, List<Point2D> points) {
        if(points.size() <= 2) return;
        var x_mean = points.stream().mapToDouble(p -> p.getX()).sum() / points.size();
        var y_mean = points.stream().mapToDouble(p -> p.getY()).sum() / points.size();
        var covar = points.stream().mapToDouble(p -> (p.getX() - x_mean) * (p.getY() - y_mean)).sum();
        var x_var = points.stream().mapToDouble(p -> Math.pow(p.getX() - x_mean, 2)).sum();
        var beta = covar / x_var;
        var alpha = y_mean - beta * x_mean;
        var xo = (int)Math.round(points.get(0).getX());
        var yo = (int)Math.round(alpha + beta * xo);
        var xe = (int)Math.round(points.get(points.size() - 1).getX());
        var ye = (int)Math.round(alpha + beta * xe);
        g.drawLine(xo, yo, xe, ye);
    }    
}
