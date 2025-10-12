package kerlib.graphs;

public class AxisNumber<T extends Number> extends Axis<T>{
    /**Форматирование чисел*/
    java.util.function.Function<Number, String> format;

    public AxisNumber(String name, String unit) {
        super(name, unit);
    }
    @Override
    protected double transformLocal(T v) {
        return v.doubleValue();
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
