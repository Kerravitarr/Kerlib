/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;

/** Точечная с линейными кривыми и маркерами
 * @author Kerravitarr (github.com/Kerravitarr)
 */
public class DirectMarkers extends GraphPrinter {
    public DirectMarkers() {
        setPP((g,x,y) -> kerlib.draw.tools.fillCircle(g, x, y, 2));
    }
    @Override
    public void draw(Graphics2D g, List<Point2D> points) {
        if(points.isEmpty()) return;
        else if(points.size() == 1) {
            var point = points.get(0);
            drawMarker(g, point);
            return;
        }
        var p = new java.awt.geom.Path2D.Double();   
        var f = points.get(0);
        p.moveTo(f.getX(), f.getY());
        points.forEach(point -> p.lineTo(point.getX(), point.getY()));
        g.draw(p);
        points.forEach(point -> drawMarker(g, point));
    }    
}
