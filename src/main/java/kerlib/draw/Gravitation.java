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
        private final java.awt.Point.Double center = new java.awt.Point.Double(Math.random()*2 - 1, Math.random()*2 - 1);
        ///Сила, действующая на узел
        public final java.awt.Point.Double forse = new java.awt.Point.Double(0, 0);
        ///Сила для  узла на прошлом ходе
        private final java.awt.Point.Double preforse = new java.awt.Point.Double(forse.x, forse.y);
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
    private double floatTmp = 0.01;
    ///Отношение ширины экрана к высоте. W/H
    private double w_h = 1;
    ///Корень из средне квадратического значения силы перемеещения
    private double avrforse = 0;
    ///Минимальная скорость перемещения узлов
    private double min_v = 0.001;

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
        min_v = 2 / Math.max(width, height);
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
        double preehyp;
        while(true){
            var h = gravitation();
            if(ehyp == 0) ehyp = Math.max(h, 100); //100 - чтобы маленькие флуктуации тоже обновились
            else {
                preehyp = ehyp;
                ehyp = ehyp *(1 - 0.01) + h * 0.01;
                if(h == 0 || preehyp == ehyp || ehyp <= h ) {
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
                first.preforse.x = first.forse.x;
                first.preforse.y = first.forse.y;
                first.forse.x = first.forse.y = 0;
                var secondi = nodes.iterator();
                for (var j = 0; secondi.hasNext();j++) {
                    if(i == j) continue;
                    final var second = tp.apply(secondi.next());
                    var dist = sub(first.center,second.center);
                    var hypotenuse = Math.hypot(dist.x, dist.y);
                    if(hypotenuse == 0) continue;
                    var fr = fr(hypotenuse, k) / hypotenuse; //Делим на hypotenuse, чтобы нормализовать вектор dist
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
                var fa = fa(hypotenuse, k) / hypotenuse; //Делим на hypotenuse, чтобы нормализовать вектор dist
                first.forse.x -= dist.x * fa;
                first.forse.y -= dist.y * fa;
                second.forse.x += dist.x * fa;
                second.forse.y += dist.y * fa;
            }
        }
        floatTmp = floatTmp * 0.9 + kerlib.tools.betwin(min_v,avrforse,0.01) * 0.1;
        //System.out.println(String.format("%.05f - %.05f", avrforse,floatTmp));
        var maxHypotenuse = 0.0;
        avrforse = 0;
        for (var o : nodes) {
            final var first = tp.apply(o);
            var hypotenuse = Math.hypot(first.forse.x, first.forse.y);
            if(!first.isFix() && Math.abs(hypotenuse) > 0){
                maxHypotenuse = Math.max(maxHypotenuse, hypotenuse);
                var step = Math.min(hypotenuse, floatTmp) / hypotenuse;
                var dx = first.forse.x*step;
                var dy = first.forse.y*step;
                if(w_h < 1){
                    first.move(kerlib.tools.betwin(-w_h, first.getCX()+dx, w_h)-first.getCX(), kerlib.tools.betwin(-1, first.getCY()+dy, 1) - first.getCY());
                } else {
                    first.move(kerlib.tools.betwin(-1, first.getCX()+dx, 1)-first.getCX(), kerlib.tools.betwin(-1/w_h, first.getCY()+dy, 1/w_h) - first.getCY());
                }
                //Используем векторное произведение. Чтобы два разнонаправленных или сонаправленных вектора давали 0
                avrforse += Math.pow((first.preforse.x * first.forse.y - first.preforse.y * first.forse.x),2);
                first.update();
            }
        }
        avrforse = (avrforse) / nodes.size();
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
