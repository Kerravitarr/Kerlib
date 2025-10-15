package kerlib.graphs;

import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import kerlib.draw.tools;
import kerlib.draw.tools.alignmentX;
import kerlib.draw.tools.alignmentY;

public class AxisNumber<T extends Number> extends Axis<T>{
    ///Формат вывода чисел
    private final DecimalFormatSymbols SYMBOLS;
    ///Функция отрисовки графика
    private java.util.function.Function<Boolean,String> printer;
    
    
    public AxisNumber(String name) {this(name, "");}
    public AxisNumber(String name, String unit) {
        super(name, unit);

        Locale locale = Locale.getDefault();
        SYMBOLS = new DecimalFormatSymbols(locale);
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
    
    @Override
    public int maxWidth(Graphics2D g2d, int width, int charWidth, Printer printer){
        if(maximum == minimum){
            var power = (int)Math.floor(Math.log10(maximum));
            var formatter = new DecimalFormat(getNumberFormat(power), SYMBOLS);
            var text = formatter.format(maximum);
            printer.set((y0) -> printer.print(y0+width/2,text,alignmentY.center));
            return tools.getTextWidth(g2d, text);
        }
        var previous = 1;
        main_loop:for(var ten = 0; ; ten++){
            for(var dig = 1; dig <= 9; dig++){
                //Количество делений.
                var count = (int) Math.floor(dig * Math.pow(10, ten)); 
                //Всегда 2 в запасе, чтобы минимум и максимум вместить гарантированно
                if(charWidth * (count + 2) > width){
                    break main_loop;
                } else {
                    previous = count;
                }
            }
        }
        if(previous == 1){
            var val = (minimum + maximum) / 2;
            var power = (int)Math.floor(Math.log10(val));
            var formatter = new DecimalFormat(getNumberFormat(power), SYMBOLS);
            var text = formatter.format(val);
            printer.set((y0) -> printer.print(y0+width/2,text,alignmentY.center));
            return tools.getTextWidth(g2d, text);
        }
        //Теперь мы знаем, на сколько делений максимум мы можем поделить нашу ось
        var range = maximum - minimum;
        var tickInterval = range / (previous-1);
        //Но надо найти такой шаг между делениями, чтобы он был красивым. 
        //То есть был в виде n*10^k
        var power = (int)Math.floor(Math.log10(range));
        var previosK = power;
        var n = 0;
        var k = 0;
        WH: while (true) {
            var base = Math.pow(10, power);
            var bestNumber = 1;
            var minError = Double.MAX_VALUE;
            for (var multiplier : new int[]{1, 2, 5}) {
                var tick = base * multiplier;
                if (tick <= 0) continue;
                var error = Math.abs(tick - tickInterval);
                if (error < minError) {
                    minError = error;
                    bestNumber = multiplier;
                }
            }
            var pc = power;
            switch (bestNumber) {
                case 1 -> {
                    if(previosK >= power)
                       --power;
                    else {
                        var nb = Math.pow(10, power-1);
                        var tick = nb * 5;
                        if(tick > 0 && Math.abs(tick - tickInterval) < minError){
                            n = 5;
                            k = power-1;
                            break WH;
                        } else {
                            n = bestNumber;
                            k = power;
                            break WH;
                        }
                    }
                }
                case 2 -> {
                    n = bestNumber;
                    k = power;
                    break WH;
                }
                case 5 -> {
                    if(previosK <= power)
                       ++power;
                    else {
                        var nb = Math.pow(10, power+1);
                        var tick = nb * 1;
                        if(tick > 0 && Math.abs(tick - tickInterval) < minError){
                            n = 1;
                            k = power+1;
                            break WH;
                        } else {
                            n = bestNumber;
                            k = power;
                            break WH;
                        }
                    }
                }
            }
            previosK = pc;
        }
        var idealTickInterval = n * Math.pow(10, k);
        var min = minimum > 0 ? Math.ceil(minimum/idealTickInterval)*idealTickInterval : Math.floor(minimum/idealTickInterval)*idealTickInterval;
        var max = maximum < 0 ? Math.ceil(maximum/idealTickInterval)*idealTickInterval : Math.floor(maximum/idealTickInterval)*idealTickInterval;
        var formatter = new DecimalFormat(getNumberFormat(k), SYMBOLS);
        printer.set((y0) -> {
            for(var i = min; i < max; i += idealTickInterval){
                printer.print(y0+width-(i-minimum)*width/range,formatter.format(i));
            }
        });
        return Math.max(tools.getTextWidth(g2d, formatter.format(min)),tools.getTextWidth(g2d, formatter.format(max)));
    }
    public void printTicks(Graphics2D g2d, int width, int charWidth){

    }
    private static String getNumberFormat(int k) {
        if (k < 0) {
            return "0." + "0".repeat(-k);
        } else {
            return "#"; // Без десятичных знаков
        }
    }

    @Override
    public String toString() {
        return String.format("%s%f;%f%s,%s%s", isAutoresizeMin ? "<-" : "[", minimum, maximum, isAutoresizeMax ? "->" : "]", name, unit);
    }
}
