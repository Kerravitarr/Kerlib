/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

import kerlib.graphs.ltypes.DottedSmoothMarkers;
import java.util.ArrayList;
import java.util.List;

import javax.swing.event.EventListenerList;

/**Сам график с даннымми*/
public class Graph<T,XT,YT> {

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
    ///Добавляет стили к текущему графику
    ///@param add стиль, который будет применён прежде чем отрисовать график
    ///@return этот же самый график
    public Graph<T,XT,YT> style(GraphStyle add){
        this.styles.add(add);
        fireChangeEvent();
        return this;
    }
    ///@return Возвращает объект, который будет отрисовывать график по точкам на экране
    public GraphPrinter printer(){return printer;}
    ///@param printer Объект, который будет отрисовывать график по точкам на экране
    public Graph<T,XT,YT> printer(GraphPrinter printer){this.printer = printer;fireChangeEvent(); return this;}
    ///@return Возвращает объект, который будет отрисовывать легедну
    public GraphSignatures signatures(){return signatures;}
    ///@param signatures Объект, который будет отрисовывать легенду
    public Graph<T,XT,YT> signatures(GraphSignatures signatures){this.signatures = signatures; fireChangeEvent(); return this;}
    ///@return true, если надо создать подпись для графика
    public boolean isNeedSignature(){return isNeedSignature;}
    
    
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

    ///Добавить график на панель для рисования
    void set(ChartPanel chartPanel) {
        listenerList.add(ChartPanel.class, chartPanel);
        fireChangeEvent();
    }
    ///Убрать график с панели для рисования
    void unset(ChartPanel chartPanel) {
        listenerList.remove(ChartPanel.class, chartPanel);
    }

    void draw(java.awt.Graphics2D g) {
        printer.draw(g,points,X,Y);
    }
    ///Обновляет по осям минимумы и максимумы
    void recalculate() {
        this.points.forEach(p -> {
            X.add(p.getX());
            Y.add(p.getY());
        });
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
    static class GraphUpdateEvent {}

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
    ///Объект, который будет рисовать все точки графика
    private GraphPrinter printer = new DottedSmoothMarkers();
    ///Объект, который будет создавать легенду графика
    private GraphSignatures signatures = new GraphSignatures();

    /**Функция превращения объекта в ординату X*/
    private final java.util.function.Function<T, XT> toX;
    /**Функция превращения объекта в ординату Y*/
    private final java.util.function.Function<T, YT> toY;
    ///Слушатели событий от этого графика
    private transient EventListenerList listenerList;

    /**Нужна ли подпись графика внизу?*/
    private boolean isNeedSignature = true;
}
