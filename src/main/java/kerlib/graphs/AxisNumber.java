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
    
    @Override
    public int maxWidth(Graphics2D g2d, int height,Printer printer){
        if(maximum == minimum){
            var power = (int)Math.floor(Math.log10(maximum == 0 ? 1 : maximum));
            var formatter = new DecimalFormat(getNumberFormat(power), SYMBOLS);
            var text = formatter.format(maximum);
            printer.setY(() -> printer.tick(height/2d,text,alignmentY.center), v -> height/2d, (v) -> text);
            return tools.getTextWidth(g2d, text);
        }
        var charWidth = tools.getTextHeight(g2d, "А");
        var previous = 1;
        main_loop:for(var ten = 0; ; ten++){
            for(var dig = 1; dig <= 9; dig++){
                //Количество делений.
                var count = (int) Math.floor(dig * Math.pow(10, ten)); 
                //Всегда 2 в запасе, чтобы минимум и максимум вместить гарантированно
                if(charWidth * (count + 2) > height){
                    break main_loop;
                } else {
                    previous = count;
                }
            }
        }
        if(previous == 1){
            var val = (minimum + maximum) / 2;
            var power = log10(val);
            var formatter = new DecimalFormat(getNumberFormat(power), SYMBOLS);
            var text = formatter.format(val);
            printer.setY(() -> printer.tick(height/2,text,alignmentY.center), v -> height/2d, (v) -> text);
            return tools.getTextWidth(g2d, text);
        }
        //Теперь мы знаем, на сколько делений максимум мы можем поделить нашу ось
        var range = maximum - minimum;
        var tickInterval = range / (previous-1);
        //Но надо найти такой шаг между делениями, чтобы он был красивым. 
        //То есть был в виде n*10^k
        var power = log10(range);
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
        var min = roundMin(idealTickInterval);
        var max = roundMax(idealTickInterval);
        var formatter = new DecimalFormat(getNumberFormat(k), SYMBOLS);
        {
            var hr = height/range;
            printer.setY(() -> {
                for(var i = min; i < max; i += idealTickInterval){
                    printer.tick(height-(i-minimum)*hr,formatter.format(i));
                }
            }, v -> height-(v-minimum)*hr, v -> formatter.format((height-v)/hr+minimum));
        }
        return Math.max(tools.getTextWidth(g2d, formatter.format(min)),tools.getTextWidth(g2d, formatter.format(max)));
    }    
    @Override
    public void printHorizontalTicks(Graphics2D g2d, int width, Printer printer){
        if(maximum == minimum){
            var power = log10(maximum);
            var formatter = new DecimalFormat(getNumberFormat(power), SYMBOLS);
            var text = formatter.format(maximum);
            printer.setX(() -> printer.tick(width/2,text,alignmentX.center), _ -> width/2d, _ -> text, 0);
            return;
        }
        var range = maximum - minimum;
        //А вот тут сложнее искать интервал
        //То есть был в виде n*10^k
        var power = log10(range);
        var previosK = power;
        var n = 0;
        var k = 0;
        var calculateW = (java.util.function.BiFunction<Double,Integer,Integer>)(interval,pw) -> {
            var dels = (int)(range/interval); //Количество делений, которые будут размещены на оси
            if(dels == 0)return 0;
            var max = roundMax(interval);
            var formatter = new DecimalFormat(getNumberFormat(pw), SYMBOLS);
            var w = kerlib.draw.tools.getTextWidth(g2d, formatter.format(max));
            var maxWidth = width/(dels * 2); //Расстояние между штрихами надо оставить двойное. Это очень важно, чтобы цифры не сливались!
            if(maxWidth < w) return width; //Чтобы показать, что двигаться надо в эту сторону
            else return maxWidth;
        };
        WH: while (true) {
            var base = Math.pow(10, power);
            var bestNumber = 1;
            var minError = Double.MAX_VALUE;
            for (var multiplier : new int[]{1, 2, 5}) {
                var tick = base * multiplier;
                if (tick <= 0) continue;
                var fwidth = calculateW.apply(tick, power);
                if(fwidth > 0 && fwidth <= minError){
                    minError = fwidth;
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
                        var nextW = calculateW.apply(tick, power-1);
                        if(tick > 0 && nextW > 0 && nextW < minError){
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
                        var nextW = calculateW.apply(tick, power+1);
                        if(tick > 0 && nextW > 0 && nextW < minError){
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
        var min = roundMin(idealTickInterval);
        var max = roundMax(idealTickInterval);
        var formatter = new DecimalFormat(getNumberFormat(k), SYMBOLS);
        {
            var wr = width/range;
            printer.setX(() -> {
                for(var i = min; i < max; i += idealTickInterval){
                    printer.tick((i-minimum)*wr,formatter.format(i));
                }
            }, v -> (v - minimum)*wr, v -> formatter.format(v/wr+minimum), kerlib.draw.tools.getTextWidth(g2d, formatter.format(max)));
        }
    }
    /**@return минимальное значение, которое будет отображено на оси при заданном интервале */
    private double roundMin(double idealTickInterval){
        return Math.ceil(minimum/idealTickInterval)*idealTickInterval;
    }
    /**@return максимальное значение, которое будет отображено на оси при заданном интервале */
    private double roundMax(double idealTickInterval){
        return Math.floor(maximum/idealTickInterval)*idealTickInterval;
    }
    private static String getNumberFormat(int k) {
        if (k < 0) {
            return "0." + "0".repeat(-k);
        } else {
            return "#"; // Без десятичных знаков
        }
    }
    private static int log10(double val){
        return val == 0 ? 1 : (int) Math.floor(Math.log10(val));
    }

    @Override
    public String toString() {
        return String.format("%s%f;%f%s,%s%s", isAutoresizeMin ? "<-" : "[", minimum, maximum, isAutoresizeMax ? "->" : "]", name, unit);
    }
}
