/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;

/**Сам график с даннымми*/
public class Graph<T,XT,YT> {
    ///Название графика
    public final String name;
    ///Ось X графика
    Axis<XT> X;
    ///Ось Y графика
    Axis<YT> Y;
    ///Все координаты графика
    private final List<java.awt.geom.Point2D> points = new ArrayList<>();
    ///Все стили, которые надо применить для отображения графика
    final List<GraphStyle> styles = new ArrayList<>();
    private GraphPrinter printer;

    /**Нужна ли подпись графика внизу?*/
    private boolean isNeedSignature = true;

    /**Функция превращения объекта в ординату X*/
    private final java.util.function.Function<T, XT> toX;
    /**Функция превращения объекта в ординату Y*/
    private final java.util.function.Function<T, YT> toY;
    ///Слушатели событий от этого графика
    private transient EventListenerList listenerList;

    public Graph(String n, Axis<XT> x, Axis<YT> y) {
        this(n, x, y, null,null);
    }
    public Graph(String n, Axis<XT> x, Axis<YT> y, java.util.function.Function<T, XT> tox, java.util.function.Function<T, YT> toy) {
        name = n;
        X = x;
        Y = y;
        toX = tox;
        toY = toy;
        listenerList = new EventListenerList();
    }

    /** @param o новый объект графика*/
    public void add(T o) {
        add(toX.apply(o), toY.apply(o));
    }
    /** @param x координата по оси
     * @param y координата по оси*/
    public void add(XT x, YT y) {
        var xv = X.transform(x);
        var yv = Y.transform(y);
        points.add(new java.awt.geom.Point2D.Double(xv, yv));
        fireChangeEvent();
    }
    /**Очищает график от данных*/
    public void clear() {
        points.clear();
        fireChangeEvent();
    }

    /** @param isNeedSignature нужно подписывать график внизу?*/
    public Graph<T,XT,YT> setNeedSignature(boolean isNeedSignature) {
        this.isNeedSignature = isNeedSignature;
        fireChangeEvent();
        return this;
    }
    /// @return true, если график пустой
    public boolean isEmpty() {return points.isEmpty();}

    java.awt.geom.Point2D get(int index) {
        return points.get(index);
    }

    void set(ChartPanel chartPanel) {
        listenerList.add(ChartPanel.class, chartPanel);
        fireChangeEvent();
    }

    void draw(java.awt.Graphics2D g) {
        if(printer == null) printer = new DottedSmoothMarkers();
        printer.draw(g,points,X,Y);
    }

    /**
     * Sends an {@link AxisChangeEvent} to all registered listeners.
     */
    protected void fireChangeEvent() {
        var listeners = this.listenerList.getListenerList();
        var event = new GraphUpdateEvent();
        for (var i = listeners.length - 2; i >= 0; i -= 2) {
            if (GraphChangeListener.class.isAssignableFrom((Class<?>)listeners[i])) {
                ((GraphChangeListener) listeners[i + 1]).graphChanged(event);
            }
        }
    }


    static interface GraphChangeListener {
        public void graphChanged(GraphUpdateEvent event);
    }
    static class GraphUpdateEvent {

    }

}
