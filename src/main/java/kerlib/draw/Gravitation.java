/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.util.Collection;
import java.util.Iterator;
import kerlib.json.JSON;
import java.util.List;
import java.util.function.Function;

/**
 *Класс, моделирующий гравитацию объектов на поле
 * @author zeus
 */
public class Gravitation<T> {
    ///Планета, которая и будет в нашей гравитации работать. Любой класс для гравитации должен или наслдеоваться от планеты или иметь внутренний класс для планеты
    public static class Planet {
        ///@return местоположение объекта на карте
        public int getX(){return (int) Math.round(g == null ? getCX() : (double)g.transformX.apply(getCX()));}
        ///@return местоположение объекта на карте
        public int getY(){return (int) Math.round(g == null ? getCY() : (double)g.transformY.apply(getCY()));}
        ///Сбрасывает положение планеты, отправляя её в самый центр поля
        public final void reset(){center.x = center.y = 0;}
        ///@return местоположение объекта в рамках поля [-1;1]
        public double getCX(){return center.x;}
        ///@return местоположение объекта в рамках поля [-1;1]
        public double getCY(){return center.y;}
        ///Сдвинуть точку на какое-то расстояние
        ///@param dx на сколько сдвигать точку. Это расстояние не в пикселях, а в единицах поля!
        ///@param dy на сколько сдвигать точку. Это расстояние не в пикселях, а в единицах поля!
        public void move(double dx, double dy){center.x +=dx;center.y +=dy;}
        
        ///@return Серелизованный объект, пригодный к сохранению
        public JSON toJSON(){return new JSON().add("x", center.x).add("y", center.y);}
        ///@param j объект, из которого надо восстановить данные
        public void fromJSON(JSON j){center.x = j.get(double.class,"x");center.y = j.get(double.class,"y");}
        
        ///Функция, которая вызывается, после того, как местопложение объекта будет обновлено
        protected void update(){};
        ///@return true, если объект надо зафиксировать
        protected boolean isFix(){return false;}
        
        ///Местоположение узла
        private java.awt.Point.Double center = new java.awt.Point.Double(Math.random()*2 - 1, Math.random()*2 - 1);
        ///Текущая скорость узла
        private java.awt.Point.Double speed = new java.awt.Point.Double(0, 0);
        ///Сила, действующая на узел
        private java.awt.Point.Double forse = new java.awt.Point.Double(0, 0);
        ///Гравитация, в которой мы вращаемся
        private Gravitation g;
    }
    ///Функция получения данных по конкретной планете
    private final java.util.function.Function<T,? extends Planet> tp;
    ///Функция получения друзей планеты. Другими словами - тех узлов, с которыми она связана
    private final java.util.function.Function<T,Collection<T>> friends;
    ///Функция получения из относительных координат - абсолютные
    private java.util.function.Function<Double, Double> transformX;
    ///Функция получения из относительных координат - абсолютные
    private java.util.function.Function<Double, Double> transformY;
    ///Все узлы, с которыми мы работаем. Сылка на реальный массив
    private Collection<T> nodes;
    ///Температура, нужная, чтобы граф всегда немного "дышал". Он так лучше находит минимум
    private double floatTmp = 0;
    ///Минимальная и максимальная гипотинузы на прошлом шаге моделирования
    private double minHyp = 0;
    ///Минимальная и максимальная гипотинузы на прошлом шаге моделирования
    private double maxHyp = 0;
    ///Отношение ширины экрана к высоте. W/H
    private double w_h = 1;

    ///Создать обработчик гравитацаа
    /// @param transformX Функция получения из относительных координат - абсолютные
    /// @param transformY Функция получения из относительных координат - абсолютные
    ///@param tp функция получения из объекта списка, объект планеты
    ///@param friends функция получения друзей планеты, для отображения взаимодействия с ней
    public Gravitation(java.util.function.Function<Double, Double> transformX, java.util.function.Function<Double, Double> transformY, Function<T, Planet> tp, Function<T, Collection<T>> friends) {
        this.tp = tp;
        this.friends = friends;
        this.transformX = transformX;
        this.transformY = transformY;
    }
    ///Создание обработчика гравитации для объекта - наследника планеты
    /// @param <V> текущий класс объекта, являющийся наследником планеты
    /// @param transformX Функция получения из относительных координат - абсолютные
    /// @param transformY Функция получения из относительных координат - абсолютные
    /// @param friends функция получения друзей планеты, для отображения взаимодействия с ней
    public <V extends Planet>Gravitation(java.util.function.Function<Double, Double> transformX, java.util.function.Function<Double, Double> transformY, Function<V, Collection<V>> friends){
        this(transformX,transformY, v -> (Planet)v, f -> (Collection<T>)friends.apply((V)f));
    }
    ///Создание обработчика гравитации для объекта - наследника планеты
    /// @param friends функция получения друзей планеты, для отображения взаимодействия с ней
    public Gravitation(Function<T, Collection<T>> friends){
        this(v->v,v->v, v -> (Planet)v, f -> (Collection<T>)friends.apply((T)f));
    }
    ///@param nodes узлы, которые мы будем обрабатывать
    public void set(Collection<T> nodes){
        this.nodes = nodes;
    }
    ///Сохранить пропорции мира. Нужно, чтобы объекты за эти пределы не вылетали
    ///@param width ширина мира
    ///@param height высота мира
    public void set(double width, double height){
        w_h = width / height;
    }
    ///Сохранить функции преобразования для мира
    public void set(java.util.function.Function<Double, Double> transformX, java.util.function.Function<Double, Double> transformY){
        this.transformX = transformX;
        this.transformY = transformY;
    }
    /// Инициализировать гравитацию, расставить объекты в изначальных местах
    /// @param nodes узлы, которые надо обработать
    public void init(Collection<T> nodes){
        set(nodes);
        init();
    }
    /// Инициализировать гравитацию, расставить объекты в изначальных местах
    public void init(){
        set(nodes);
        var ehyp = 0d;
        while(true){
            var h = gravitation();
            if(ehyp == 0) ehyp = h;
            else {
                ehyp = ehyp *(1 - 0.01) + h * 0.01;
                if(h == 0 || h - ehyp >= 0.0001 ) {
                    break;
                }
            }
        }
    }
    ///Обработать воздействие графитаци
    /// @param nodes узлы, которые надо обработать
    /// @return максимальное расстояние от центра, или где находится самый дальний узел
    public double gravitation(List<T> nodes){
        set(nodes);
        return gravitation();
    }
    ///Обработать воздействие гравитаци
    /// @return максимальное расстояние от центра, или где находится самый дальний узел
    public double gravitation(){
        var k = Math.sqrt(1d / nodes.size());
        {
            var main = nodes.iterator();
            for (var i = 0; main.hasNext();i++) {
                var first = tp.apply(main.next());
                first.g = this;
                first.forse.x = first.forse.y = 0;
                var secondi = nodes.iterator();
                for (var j = 0; secondi.hasNext();j++) {
                    if(i == j) continue;
                    final var second = tp.apply(secondi.next());
                    var dist = sub(first.center,second.center);
                    var hypotenuse = Math.hypot(dist.x, dist.y);
                    if(hypotenuse == 0) continue;
                    dist.x /= hypotenuse;
                    dist.y /= hypotenuse;
                    var fr = fr(hypotenuse, k);
                    first.forse.x += dist.x * fr;
                    first.forse.y += dist.y * fr;
                }
            }
        }
        for (var o : nodes) {
            final var first = tp.apply(o);
            for(var j : friends.apply(o)){
                final var second = tp.apply(j);
                var dist = sub(first.center,second.center);
                var hypotenuse = Math.hypot(dist.x, dist.y);
                if(hypotenuse == 0) continue;
                dist.x /= hypotenuse;
                dist.y /= hypotenuse;
                var fa = fa(hypotenuse, k);
                first.forse.x -= dist.x * fa;
                first.forse.y -= dist.y * fa;
                second.forse.x += dist.x * fa;
                second.forse.y += dist.y * fa;
            }
        }
        floatTmp = kerlib.tools.betwin(0, floatTmp + (Math.random() - 0.5) * (maxHyp - minHyp), Math.min(maxHyp, 0.01));
        var maxHypotenuse = 0.0;
        var minHypotenuse = Double.MAX_VALUE;
        for (var o : nodes) {
            final var first = tp.apply(o);
            var hypotenuse = Math.hypot(first.forse.x, first.forse.y);
            if(!first.isFix() && Math.abs(hypotenuse) > 0.01){
                maxHypotenuse = Math.max(maxHypotenuse, hypotenuse);
                minHypotenuse = Math.min(minHypotenuse, hypotenuse);
                first.forse.x /= hypotenuse;
                first.forse.y /= hypotenuse;
                first.move(first.forse.x*Math.min(hypotenuse, floatTmp), first.forse.y*Math.min(hypotenuse, floatTmp));
                if(w_h < 1){
                    first.center.x = kerlib.tools.betwin(-w_h, first.center.x, w_h);
                    first.center.y = kerlib.tools.betwin(-1, first.center.y, 1);
                } else {
                    first.center.x = kerlib.tools.betwin(-1, first.center.x, 1);
                    first.center.y = kerlib.tools.betwin(-1/w_h, first.center.y, 1/w_h);
                }
                first.update();
            }
        }
        minHyp = minHypotenuse;
        return maxHyp = maxHypotenuse;
    }
    ///Вычисляет силу отталкивания между двумя узлами
    ///@param dist расстояние между двумя узлаими
    ///@param k корень из площади делённой на количество объектов
    private static double fr(double dist, double k) {
        return Math.pow(k, 2) / dist;
    }
    ///Вычисляет силу притягивания между двумя узлами
    ///@param dist расстояние между двумя узлаими
    ///@param k корень из площади делённой на количество объектов
    private static double fa(double dist, double k) {
        return Math.pow(dist, 2) / k;
    }
    ///Находит разницу между двумя точками. Вектор направленный из A в B
    private static java.awt.Point.Double sub(java.awt.Point.Double a, java.awt.Point.Double b){
        return new java.awt.Point.Double(a.x - b.x, a.y - b.y);
    }
}
