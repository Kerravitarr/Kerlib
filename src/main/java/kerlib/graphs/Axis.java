/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

/**Ось графикa*/
public abstract class Axis<T> {
    ///Название оси
    public final String name;
    ///Единицы измерения, если есть
    public final String unit;

    /**Нужна ли подпись оси сбоку?*/
    boolean isNeedSignature = true;
    /**Нужно автоматически расширять ось по минимуму?*/
    private boolean isAutoresizeMin = true;
    /**Нужно автоматически расширять ось по максимуму?*/
    private boolean isAutoresizeMax = true;

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
    }

}
