/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

import java.awt.Color;
import java.awt.Stroke;
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
    ///Все наши точки графика
    private final List<T> objects = new ArrayList<>();
    ///Все координаты графика
    private final List<java.awt.geom.Point2D> points = new ArrayList<>();

    /**Цвет графика*/
    private Color color = Color.BLACK;
    /**Форма графика*/
    private Stroke stroke = null;
    /**Нужна ли подпись графика внизу?*/
    private boolean isNeedSignature = true;
    /**Функция превращения объекта в ординату X*/
    private final java.util.function.Function<T, XT> toX;
    /**Функция превращения объекта в ординату Y*/
    private final java.util.function.Function<T, YT> toY;
    ///Панель, на которой рисуется этот график
    private ChartPanel chart;
    ///Слушатели событий от этого графика
    private transient EventListenerList listenerList;

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
        objects.add(o);
        var x = X.transform(toX.apply(o));
        var y = Y.transform(toY.apply(o));
        points.add(new java.awt.geom.Point2D.Double(x, y));
        fireChangeEvent();
    }
    /**Очищает график от данных*/
    public void clear() {
        objects.clear();
        points.clear();
        fireChangeEvent();
    }

    /** @param c цвет графика*/
    public void setColor(Color c) {
        color = c;
        fireChangeEvent();
    }

    /** @param s оформление графика*/
    public void setStroke(Stroke s) {
        stroke = s;
        fireChangeEvent();
    }

    /** @param isNeedSignature нужно подписывать график внизу?*/
    public void setNeedSignature(boolean isNeedSignature) {
        this.isNeedSignature = isNeedSignature;
        fireChangeEvent();
    }

    java.awt.geom.Point2D get(int index) {
        return points.get(index);
    }

    public void set(ChartPanel chartPanel) {
        this.chart = chartPanel;
        listenerList.add(ChartPanel.class, chartPanel);
        fireChangeEvent();
    }

    /**
     * Sends an {@link AxisChangeEvent} to all registered listeners.
     */
    protected void fireChangeEvent() {
        var listeners = this.listenerList.getListenerList();
        for (var i = listeners.length - 2; i >= 0; i -= 2) {
            /*if (listeners[i] == AxisChangeListener.class) {
                ((AxisChangeListener) listeners[i + 1]).axisChanged(event);
            }*/
        }
    }

}
