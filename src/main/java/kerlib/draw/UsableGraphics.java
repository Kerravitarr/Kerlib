/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.awt.Graphics;

import kerlib.draw.tools.alignmentX;
import kerlib.draw.tools.alignmentY;

/**Мой собственный класс для графики
 * Добавляет к станадртной графике те элементы, которых мне очень не хватает
 * @author ilia
 */
public class UsableGraphics {
    private final Graphics g;

    public UsableGraphics(Graphics g) {
        this.g = g;
    }
    ///Рисует овал с центром в заданных координатах
    ///@param cx центр по оси х
    ///@param cy центр по оси y
    ///@param width ширина
    ///@param height высота
    /// @return текущий объект
    public UsableGraphics drawOvalByCenter(int cx, int cy, int width, int height){
        g.drawOval(cx-width/2, cy-height/2, width, height);
        return this;
    }
    ///Рисует круг с центром в заданных координатах
    ///@param cx центр по оси х
    ///@param cy центр по оси y
    ///@param diameter диаметр
    /// @return текущий объект
    public UsableGraphics drawOvalByCenter(int cx, int cy, int diameter){
        return drawOvalByCenter(cx,cy,diameter,diameter);
    }
	///Рисует стрелку
	/// @param x0 - положение
    /// @param y0
	/// @param d - направление
	/// @param lenght - длина усов
	/// @param angl - угол между стрелками, в градусах
    /// @return текущий объект
    public UsableGraphics arrow(double x0, double y0, tools.derect d, double lenght, double angl){
        kerlib.draw.tools.arrow(g, x0, y0, d, lenght, angl);
        return this;
    }
	///Рисует стрелку
	/// @param x0 - положение
    /// @param y0
	/// @param d - направление, в радианах
	/// @param lenght - длина усов
	/// @param angl - угол между стрелками, в градусах
    /// @return текущий объект
    public UsableGraphics arrow(double x0, double y0, double d, double lenght, double angl){
		double an = d + angl;
		drawLine(x0, y0, lenght * Math.cos(an) + x0, lenght * Math.sin(an) + y0);
		an = d - angl;
		drawLine(x0, y0, lenght * Math.cos(an) + x0, lenght * Math.sin(an) + y0);
        return this;
    }
	///Рисует линию
	/// @param x1
    /// @param y1
	/// @param x2
    /// @param y2
    /// @return текущий объект
    public UsableGraphics drawLine(int x1, int y1, int x2, int y2){
        g.drawLine(x1, y1, x2, y2);
        return this;
    }
	///Рисует линию
	/// @param x1
    /// @param y1
	/// @param x2
    /// @param y2
    /// @return текущий объект
    public UsableGraphics drawLine(double x1, double y1, double x2, double y2){
        return drawLine(kerlib.tools.round(x1), kerlib.tools.round(y1), kerlib.tools.round(x2), kerlib.tools.round(y2));
    }
    public UsableGraphics string(double x, double y,String text, alignmentX alX, alignmentY alY){
        kerlib.draw.tools.drawString(g, x, y, text, alX, alY);
        return this;
    }
}
