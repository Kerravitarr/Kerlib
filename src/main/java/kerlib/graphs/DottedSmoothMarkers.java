/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;

/** Точечная с гладкими кривыми и маркерами
 * @author Kerravitarr (github.com/Kerravitarr)
 */
public class DottedSmoothMarkers extends GraphPrinter {
    public DottedSmoothMarkers() {
        setPP((g,x,y) -> kerlib.draw.tools.fillCircle(g, x, y, 2));
    }
    @Override
    public void draw(Graphics2D g, List<Point2D> points) {
        if(points.isEmpty()) return;
        else if(points.size() == 1) {
            var point = points.get(0);
            drawMarker(g, point);
            return;
        } else if(points.size() == 2) {
            g.drawLine((int)points.get(0).getX(),(int)points.get(0).getY(),(int)points.get(1).getX(),(int)points.get(1).getY());
            for(var point : points) 
                drawMarker(g,point);
            return;
        }
        var p = new java.awt.geom.Path2D.Double();     
        var n = points.size() - 1;   
        for (int i = 0; i < n; i++) {
            var px = i == 0 ? (2*points.get(i).getX() - points.get(i+1).getX()) : points.get(i-1).getX();
            var py = i == 0 ? (2*points.get(i).getY() - points.get(i+1).getY()) : points.get(i-1).getY();
            var cx = points.get(i).getX();
            var cy = points.get(i).getY();
            var nx = points.get(i+1).getX();
            var ny = points.get(i+1).getY();
            var nnx = i == n-1 ? nx : points.get(i+2).getX();
            var nny = i == n-1 ? ny : points.get(i+2).getY();

            // Рассчитываем контрольные точки для кривой Безье
            var cp1x = cx + (nx - px) * 0.33;
            var cp1y = cy + (ny - py) * 0.33;
            var cp2x = nx - (nnx - cx) * 0.33;
            var cp2y = ny - (nny - cy) * 0.33;
            if(i == 0) p.moveTo(cx, cy);
            p.curveTo(
                cp1x, cp1y, // Контрольная точка 1
                cp2x, cp2y, // Контрольная точка 2
                nx, ny// Конечная точка (следующая точка данных)
            );
        }
        g.draw(p);
        points.forEach(point -> drawMarker(g, point));
    }    
}
