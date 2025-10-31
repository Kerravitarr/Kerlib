/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Dimension2D;
import java.awt.geom.Rectangle2D;
import kerlib.draw.tools;

/**Класс для рисования легенды графика
 * @author Kerravitarr (github.com/Kerravitarr)
 */
public class GraphSignatures {
    ///Радиус кружка, который мы хотим нарисовать
    private final static int POINT_SIZE = 5;
    
    private static final AffineTransform affinetransform = new AffineTransform();
	private static final FontRenderContext frc = new FontRenderContext(affinetransform,true,true);  
    
    public Dimension2D getBonds(Graph<?,?,?> graph, Graphics2D g2d){
        var ts = g2d.getFont().getStringBounds(graph.name,frc).getBounds();
        return new Dimension(ts.width + POINT_SIZE + POINT_SIZE/2, ts.height);
    }
    public void drow(Graph<?,?,?> graph, Graphics2D g2d, double x0, double y0){
        var b = getBonds(graph, g2d);
        var cy = y0 + b.getHeight() / 2;
        var cx = x0 + POINT_SIZE / 2;
        graph.styles.forEach(s -> s.set(g2d));
        kerlib.draw.tools.fillCircle(g2d, cx, cy, POINT_SIZE);
        graph.styles.forEach(s -> s.unset(g2d));
        kerlib.draw.tools.drawString(g2d, x0 + POINT_SIZE + POINT_SIZE/2, y0, graph.name, tools.alignmentX.left,tools.alignmentY.bottom);
    }
}
