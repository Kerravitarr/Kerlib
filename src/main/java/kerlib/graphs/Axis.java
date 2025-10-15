/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

import java.awt.Graphics2D;

import kerlib.draw.tools.alignmentX;
import kerlib.draw.tools.alignmentY;

/**Ось графикa*/
public abstract class Axis<T> {
    ///Название оси
    public final String name;
    ///Единицы измерения, если есть
    public final String unit;

    /**Нужна ли подпись оси сбоку?*/
    boolean isNeedSignature = true;
    /**Нужно автоматически расширять ось по минимуму?*/
    protected boolean isAutoresizeMin = true;
    /**Нужно автоматически расширять ось по максимуму?*/
    protected boolean isAutoresizeMax = true;

    /**Макисмальное значение*/
    protected double maximum;
    ///Максимальное значение, верхняя граница оси
    public double maximum() {return maximum;}
    /**Минимальное значение*/
    protected double minimum;
    ///Минимальное значение, нижняя граница оси
    public double minimum() {return minimum;}

    /**Масштаб линии в текущий момент времени, пк/единицу*/
    double scale = 1;
    /**Нулевой пиксель на экране*/
    double p0 = 1;

    ///Ось пуста? На ней не задано минимумов и максимумов
    private boolean isEmpty = true;
    ///Объект, который будет выводить ось на печать
    private Printer printer;

    Axis() {
        this("", "");
    }
    Axis(String n, String u) {
        name = n;
        unit = u;
        reset();
    }
    ///Преобразует значение в число. Так как график строится только по числам!
    /// @param v объект, с которым работает ось
    /// @return значение в некоторых единицах
    double transform(T v){
        var val = transformLocal(v);
        add(val);
        return val;
    }
    ///Преобразует значение в число. Так как график строится только по числам!
    /// @param v объект, с которым работает ось
    /// @return значение в некоторых единицах
    protected abstract double transformLocal(T v);
    protected abstract int maxWidth(Graphics2D g2d, int width, int charWidth, Printer printer);

    /** @param min нужно авторасширение по минимуму?*/
    public void setMinAutoresize(boolean min) {
        setAutoresize(min, isAutoresizeMax);
    }

    /** @param max нужно авторасширение по максимуму?*/
    public void setMaxAutoresize(boolean max) {
        setAutoresize(isAutoresizeMin, max);
    }

    /**Устанавливает значение флага для авторасширения оси
     * @param min нужно авторасширение по минимуму?
     * @param max нужно авторасширение по максимуму?
     */
    public void setAutoresize(boolean min, boolean max) {
        isAutoresizeMin = min;
        isAutoresizeMax = max;
    }

    /** @param newValue очередное значение для этой оси*/
    void add(Number newValue) {
        if (isAutoresizeMin) {
            minimum = Math.min(minimum, newValue.doubleValue());
        }
        if (isAutoresizeMax) {
            maximum = Math.max(maximum, newValue.doubleValue());
        }
        isEmpty = false;
    }

    /**Сбрасывает ограничения оси*/
    void reset() {
        if (isAutoresizeMin) {
            if (isAutoresizeMax) {
                minimum = Double.MAX_VALUE;
            } else {
                minimum = maximum;
            }
        }
        if (isAutoresizeMax) {
            if (isAutoresizeMin) {
                maximum = -Double.MAX_VALUE;
            } else {
                maximum = minimum;
            }
        }
        isEmpty = isAutoresizeMax || isAutoresizeMin;
    }
    ///@return true, если для оси не заданы минимальное и максимальное значения
    public boolean isEmpty() {return isEmpty;}

    int maxWidth(Graphics2D g2d, int width, int charWidth){
        return maxWidth(g2d, width, charWidth, printer = new Printer(g2d, true));
    }
    void draw(double x, double y, boolean isLeft){
        if (printer != null){
            printer.draw(x,y, isLeft);
        }
    }

    protected class Printer {
        ///Холст, на котором рисуем
        private final Graphics2D g2d;
        ///Смещение названий. Вправо или влево. В зависимости от типа оси
        private alignmentX alX;
        ///Текущий Х. К какому значению тяготеют значения
        private double x;
        ///Функция отрисовки
        private java.util.function.Consumer<Double> drow;

        public Printer(Graphics2D g2d, boolean isLeft) {
            this.g2d = g2d;
        }
        public void set(java.util.function.Consumer<Double> c){
            drow = c;
        }
        public void print(double y, String text){
            this.print(y, text,alignmentY.top);
        }        
        public void print(double y,String text, alignmentY al){
            kerlib.draw.tools.drawString(g2d, x,y, text, alX, alignmentY.center);
        }

        public void draw(double x, double y, boolean isLeft){
            alX = isLeft ? alignmentX.right : alignmentX.left;
            this.x = x;
            if (drow != null){
                drow.accept(y);
            }
        }
    }
}
