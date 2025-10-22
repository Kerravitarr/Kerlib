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
    private Printer vertivalPrinter;
    ///Объект, который будет выводить ось на печать
    private Printer horizontalPrinter;

    Axis() {
        this("", "");
    }
    Axis(String n, String u) {
        name = n;
        unit = u;
        reset();
    }
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
    ///@return true, если для оси не заданы минимальное и максимальное значения
    public boolean isEmpty() {return isEmpty;}

    ///Преобразует значение в число. Так как график строится только по числам!
    /// @param v объект, с которым работает ось
    /// @return значение в некоторых единицах
    protected abstract double transformLocal(T v);
    /**При рисовании вертикальной оси вызывается эта функция
     * @param g2d холст
     * @param height длина оси, сколько максимум для неё отведено место
     * @param printer объект рисования. Он передаётся, чтобы в него положить функцию отрисовки подписей оси
     * @return ширина самой широкой подписи на графике
     */
    protected abstract int maxWidth(Graphics2D g2d, int height, Printer printer);
    /**При рисовании горизонатальной оси вызывается эта функция
     * @param g2d холст
     * @param width ширина оси, сколько максимум для неё отведено место
     * @param printer объект рисования. Он передаётся, чтобы в него положить функцию отрисовки подписей оси
     */
    protected abstract void printHorizontalTicks(Graphics2D g2d, int width, Printer printer);

    ///Преобразует значение в число. Так как график строится только по числам!
    /// @param v объект, с которым работает ось
    /// @return значение в некоторых единицах
    double transform(T v){
        var val = transformLocal(v);
        add(val);
        return val;
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
                maximum = -Double.MAX_VALUE;
            } else {
                minimum = maximum;
            }
        } else if (isAutoresizeMax) {
            maximum = minimum;
        }
        isEmpty = isAutoresizeMax || isAutoresizeMin;
    }
    /**Функция получения максимальной ширины подписи на оси
     * @param g2d холст
     * @param height количество пикселей, которое занимает ось
     * @param isLeft true, если ось расположена слева
     * @return максимальная ширина подписи на оси
     */
    int maxWidth(Graphics2D g2d, int height, boolean isLeft){
        return maxWidth(g2d, height, vertivalPrinter = new Printer(g2d, isLeft, true, height));
    }
    /**Функция рисования подписей на вертикальной оси
     * @param x координата начала оси
     * @param y координата начала оси
     * @param limitY координата конца подписей оси (дальше будет идти название оси)
     */
    void drawVertical(double x, double y,double limitY){
        if (vertivalPrinter != null){
            vertivalPrinter.draw(x,y,limitY);
        }
    }
    /**Функция рисования подписей на гоизонтальной оси
     * @param g2d холст
     * @param isDown true, если ось расположена внизу
     * @param x координата начала оси
     * @param width ширина оси
     * @param y координата начала оси
     * @param limitX координата конца подписей оси (дальше будет идти название оси)
     */
    void drawHorizontal(Graphics2D g2d, boolean isDown, double x, int width, double y, double limitX){
        horizontalPrinter = new Printer(g2d, isDown, false, width);
        printHorizontalTicks(g2d,width,horizontalPrinter);
        horizontalPrinter.draw(x,y,limitX);
    }
    /** Функция получения координаты на холсте для точки
     * @param point
     * @return
     */
    double x(java.awt.geom.Point2D point){return horizontalPrinter.xyOffset + horizontalPrinter.trainsformator.apply(point.getX());}
    /** Функция получения координаты на холсте для точки
     * @param point
     * @return
     */
    double y(java.awt.geom.Point2D point){return vertivalPrinter.xyOffset + vertivalPrinter.trainsformator.apply(point.getY());}

    /**Преобразует значение экрана в подпись значения для оси
     * @param x координата по оси
     * @return null, если значение не подходит под подпись
     */
    String XtoString(double x) {
        if(x < horizontalPrinter.xyOffset) return null;
        else if(x > horizontalPrinter.xyOffset+horizontalPrinter.length) return null;
        else return horizontalPrinter.toString(x);
    }   
    /**Преобразует значение экрана в подпись значения для оси
     * @param y координата по оси
     * @return null, если значение не подходит под подпись
     */
    String YtoString(double y) {
        if(y < vertivalPrinter.xyOffset) return null;
        else if(y > vertivalPrinter.xyOffset+vertivalPrinter.length) return null;
        else return vertivalPrinter.toString(y);
    }
    /**Объект рисования оси графика */
    protected class Printer {
        public static interface Drow {public void accept();}
        ///Длина штриха для подписи, в пикселях
        private static final int TICK_LENGHTH = 5;
        ///Холст, на котором рисуем
        private final Graphics2D g2d;
        ///Какая ось? Вертикальная или горизонтальная?
        private final boolean isVertical;
        ///Верхняя граница по оси, выше неё нельзя рисовать
        private double limitXYAxis;
        ///Значение для оси. Если это вертикальная ось - X, если горизонтальная - Y
        private double xyAxisLine;
        ///Смещение по оси для первого значения (откуда ось начинается)
        private double xyOffset;
        ///Функция отрисовки
        private Drow drow;
        ///Функция преобразования координат
        private java.util.function.Function<Double,Double> trainsformator;
        ///Функция обратного преобразования точки с экрана в подпись
        private java.util.function.Function<Double,String> toString;

        ///Выравнивание текста по оси Х для вертикальной оси
        private final alignmentX alXForVertical;
        ///Выравнивание текста по оси Y для горизонтальной оси
        private final alignmentY alНForHorizontal;
        ///Длина оси, в пикселях
        private final double length;
        ///Максимальный размер подписи на оси
        private int maxTickText;
        ///Эта ось расположена слева или внизу
        private final boolean isLeftOrDown;
        /**
         * @param g2d холст
         * @param isLeftOrDown эта ось расположена слева или внизу
         * @param isVertical true, если ось вертикальная
         */
        public Printer(Graphics2D g2d, boolean isLeftOrDown, boolean isVertical, double length) {
            this.g2d = g2d;
            this.isVertical = isVertical;
            this.length = length;
            alXForVertical = isLeftOrDown ? alignmentX.right : alignmentX.left;
            alНForHorizontal = isLeftOrDown ? alignmentY.bottom : alignmentY.top;
            this.isLeftOrDown = isLeftOrDown;
        }
        /** @param c функция отрисовки значений оси. В качестве параметра передаётся ордината начала оси*/
        public void setY(Drow c,java.util.function.Function<Double,Double> t, java.util.function.Function<Double,String> toStr){
            drow = c;
            trainsformator = t;
            toString = toStr;
            maxTickText = kerlib.draw.tools.getTextHeight(g2d, "А");
        }
        /** @param c функция отрисовки значений оси. В качестве параметра передаётся ордината начала оси*/
        public void setX(Drow c,java.util.function.Function<Double,Double> t, java.util.function.Function<Double,String> toStr, int maxText){
            drow = c;
            trainsformator = t;
            toString = toStr;
            maxTickText = maxText;
        }
        /**Функция рисования подписей на горизонтальной оси
         * @param x координата, где надо разместить подпись
         * @param text текст подписи
         * @param alX выравнивание
         */
        public void tick(double x, String text, alignmentX alX) {
            tick(x, text, alX, alНForHorizontal);
        }
        /**Функция рисования подписей на вертикальной оси
         * @param y координата, где надо разместить подпись
         * @param text текст подписи
         * @param alY выравнивание
         */
        public void tick(double y, String text, alignmentY alY) {
            tick(y, text, alXForVertical, alY);
        }
        /**Фукнция рисования подписей на оси. Вырванивание используется по умолчанию
         * @param xy координата, где надо разместить подпись
         * @param text текст подписи
         */
        public void tick(double xy, String text) {
            if(isVertical) tick(xy, text, alignmentY.top);
            else           tick(xy, text, alignmentX.left);
        }
        /**Функция отрисовки оси
         * @param x координата начала оси
         * @param y координата начала оси
         * @param limitXY предельное значение по оси
         */
        public void draw(double x, double y, double limitXY){
            limitXYAxis = limitXY;
            if(isVertical){
                this.xyAxisLine = x;
                xyOffset = y;
                if (drow != null){
                    drow.accept();
                }
            } else {
                this.xyAxisLine = y;
                xyOffset = x;
                if (drow != null){
                    drow.accept();
                }
            }
        }
        ///Функция для возврата подписи оси на заданном значении на экране
        public String toString(double xy) {
            return toString.apply(xy - xyOffset);
        }
        /**Функция рисования подписей на оси
         * @param xy координата, где надо разместить подпись
         * @param text текст подписи
         * @param alX выравнивание по x
         * @param alY выравнивание по y
         */
        private void tick(double xy,String text, alignmentX alX, alignmentY alY){
            xy += xyOffset;
            var rxy = (int)Math.round(xy);
            var rmax = (int)Math.round(xyAxisLine);
            var dir = isLeftOrDown ? -1 : 1;
            if(isVertical){
                if(xy >= limitXYAxis+maxTickText){
                    var offset = dir*TICK_LENGHTH;
                    kerlib.draw.tools.drawString(g2d, xyAxisLine+offset, xy, text, alX, alY);
                    g2d.drawLine(rmax, rxy, (int)(xyAxisLine+offset), rxy);
                }
            } else {
                var offset = dir*TICK_LENGHTH;
                if(xy < limitXYAxis-maxTickText){
                    kerlib.draw.tools.drawString(g2d, xy,xyAxisLine-offset, text, alX, alY);
                    g2d.drawLine(rxy, rmax, rxy, (int)(xyAxisLine-offset));
                } else {
                    var w = kerlib.draw.tools.getTextWidth(g2d, text);
                    if(xy < limitXYAxis-w*2){
                        kerlib.draw.tools.drawString(g2d, xy,xyAxisLine-offset, text, alX, alY);
                        g2d.drawLine(rxy, rmax, rxy, (int)(xyAxisLine-offset));
                    }
                }
            }
        }
    }
}
