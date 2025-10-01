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
    double maximum;
    /**Минимальное значение*/
    double minimum;
    /**Форматирование чисел*/
    java.util.function.Function<Number, String> format;
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
        setFormat(new java.text.DecimalFormat());
        reset();
    }
    abstract double transform(T v);

    /**Сохраняет минимальное значение оси
     * @param min
     */
    public void setMin(Number min) {
        add(min);
        minimum = min.doubleValue();
        setMinAutoresize(false);
    }

    /**Сохраняет максимальное значение оси и отменяет авторасширение в эту сторону
     * @param max
     */
    public void setMax(Number max) {
        add(max);
        maximum = max.doubleValue();
        setMaxAutoresize(false);
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

    /** @param decimalFormatPattern формат отображения чисел оси*/
    public void setFormat(String decimalFormatPattern) {
        setFormat(new java.text.DecimalFormat(decimalFormatPattern));
    }

    /** @param decimalFormatPattern формат отображения чисел оси*/
    public void setFormat(java.text.DecimalFormat decimalFormatPattern) {
        java.text.DecimalFormat dformat = decimalFormatPattern;
        /*final var formatSymbols = new java.text.DecimalFormatSymbols(getLocale());
        formatSymbols.setDecimalSeparator('.');
        formatSymbols.setGroupingSeparator(' ');
        dformat.setDecimalFormatSymbols(formatSymbols);*/
        format = v -> dformat.format(v);
    }

    /** @param f функция преобразования числа в его текстовое описание*/
    public void setFormat(java.util.function.Function<Number, String> f) {
        format = f;
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

    /** Преобразует значение
     * @param v значение в единицах оси
     * @return значение в пикселях
     */
    double toPx(double v) {
        if (p0 > 0) {
            return p0 + (v - minimum) * scale;
        } else {
            return -p0 - (v - minimum) * scale;
        }
    }

    /** Преобразует значение
     * @param v значение в пикселях
     * @return значение в единицах оси
     */
    double toVal(double v) {
        if (p0 > 0) {
            return (v - p0) / scale + minimum;
        } else {
            return (-v - p0) / scale + minimum;
        }
    }

    /** @return true, если ось ещё ни одного значения не получила*/
    boolean isEmpty() {
        return minimum == Double.MAX_VALUE && maximum == -Double.MAX_VALUE;
    }

    @Override
    public String toString() {
        return String.format("%s%s;%s%s,%s%s", isAutoresizeMin ? "<-" : "[", format.apply(minimum), format.apply(maximum), isAutoresizeMax ? "->" : "]", name, unit);
    }

}
