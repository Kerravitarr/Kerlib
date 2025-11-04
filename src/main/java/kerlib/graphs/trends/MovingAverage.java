/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs.trends;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.List;
import kerlib.graphs.GraphPrinter;

/** Скользящее среднее
 * @author Kerravitarr (github.com/Kerravitarr)
 */
public class MovingAverage extends GraphPrinter {
    ///Ширина окна для сглаживания
    private final int windowSize;
    public MovingAverage(int windowSize) {this.windowSize = windowSize;}
    @Override
    public void draw(Graphics2D g, List<Point2D> points) {
        if(points.size() <= windowSize + 1) return; //Мало точек
        
        var px = 0;
        var py = 0;
        for (int i = 0; i <= points.size() - windowSize; i++) {
            double sumX = 0, sumY = 0;
            // Суммируем координаты в окне
            for (int j = 0; j < windowSize; j++) {
                var p = points.get(i + j);
                sumX += p.getX();
                sumY += p.getY();
            }
            // Вычисляем среднее
            var avgX = (int) Math.round(sumX / windowSize);
            var avgY = (int) Math.round(sumY / windowSize);

            if(i != 0){
                g.drawLine(px, py, avgX, avgY);
            }
            px = avgX;
            py = avgY;
        }
    }    
}
