/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

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
        
        ///Функция, которая вызывается, когда объект надо обновить
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
    private final java.util.function.Function<T,List<T>> friends;
    ///Функция получения из относительных координат - абсолютные
    private final java.util.function.Function<Double, Double> transformX;
    ///Функция получения из относительных координат - абсолютные
    private final java.util.function.Function<Double, Double> transformY;
    ///Все узлы, с которыми мы работаем. Сылка на реальный массив
    private List<T> nodes;

    ///Создать обработчик гравитацаа
    /// @param transformX Функция получения из относительных координат - абсолютные
    /// @param transformY Функция получения из относительных координат - абсолютные
    ///@param tp функция получения из объекта списка, объект планеты
    ///@param friends функция получения друзей планеты, для отображения взаимодействия с ней
    public Gravitation(java.util.function.Function<Double, Double> transformX,java.util.function.Function<Double, Double> transformY, Function<T, Planet> tp, Function<T, List<T>> friends) {
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
    public <V extends Planet>Gravitation(java.util.function.Function<Double, Double> transformX,java.util.function.Function<Double, Double> transformY, Function<V, List<V>> friends){
        this(transformX,transformY, v -> (Planet)v, f -> (List<T>)friends.apply((V)f));
    }
    ///@param nodes узлы, которые мы будем обрабатывать
    public void set(List<T> nodes){
        this.nodes = nodes;
    }
    /// Инициализировать гравитацию, расставить объекты в изначальных местах
    /// @param nodes узлы, которые надо обработать
    public void init(List<T> nodes){
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
                if(h == 0 || ehyp <= h) {
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
    ///Обработать воздействие графитаци
    /// @return максимальное расстояние от центра, или где находится самый дальний узел
    public double gravitation(){
        //var area = width * height;
        //var k = Math.sqrt(area / nodes.size());
        var k = Math.sqrt(1d / nodes.size());
        for (int i = 0; i < nodes.size(); i++) {
            final var first = tp.apply(nodes.get(i));
            first.g = this;
            first.forse.x = first.forse.y = 0;
            for (int j = 0 ; j < nodes.size(); j++) {
                if(i == j) continue;
                final var second = tp.apply(nodes.get(j));
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
        for (int i = 0; i < nodes.size(); i++) {
            final var first = tp.apply(nodes.get(i));
            for(var j : friends.apply(nodes.get(i))){
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
        var tmp = Math.random() * 0.001;
        var maxHypotenuse = 0.0;
        for (int i = 0; i < nodes.size(); i++) {
            final var first = tp.apply(nodes.get(i));
            var hypotenuse = Math.hypot(first.forse.x, first.forse.y);
            if(!first.isFix() && Math.abs(hypotenuse) > 0.01){
                maxHypotenuse = Math.max(maxHypotenuse, hypotenuse);
                first.forse.x /= hypotenuse;
                first.forse.y /= hypotenuse;
                first.move(first.forse.x*Math.min(hypotenuse, tmp), first.forse.y*Math.min(hypotenuse, tmp));
            }
            first.update();
        }
        return maxHypotenuse;
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
