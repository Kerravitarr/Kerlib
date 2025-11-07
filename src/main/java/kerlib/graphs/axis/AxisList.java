package kerlib.graphs.axis;

import java.awt.Graphics2D;
import java.util.Collection;

import kerlib.draw.tools;
import kerlib.graphs.Axis;

///Ось для списка значений
/// 
/// @author Kerravitarr (github.com/Kerravitarr)
public class AxisList<T> extends Axis<T>{    
    public AxisList(String name, Collection<T> values) {this(name, "",values);}
    public AxisList(String name, String unit, Collection<T> values) {
        this(name, unit, values, v -> v.toString());
    }
    public AxisList(String name, String unit, Collection<T> values, java.util.function.Function<T,String> toString) {
        super(name, unit);
        this.values = values;
        this.toString = toString;
    }
    @Override
    protected double transformLocal(T v) {
        var index = 0;
        for(var i = values.iterator(); i.hasNext();++index){
            if(i.next().equals(v))
                return index;
        }
        return -1;
    }
    
    @Override
    public int maxWidth(Graphics2D g2d, int height,Printer printer){
        var charWidth = tools.getTextHeight(g2d, "А");
        var previous = Math.floor(height / (charWidth*2));
        if(maximum == minimum || previous <= 1){
            return printer.setY(format(maximum), height);
        }
        //Теперь мы знаем, на сколько делений максимум мы можем поделить нашу ось
        var range = maximum - minimum;
        var tickInterval = range / (previous-1);
        //Но надо найти такой шаг между делениями, чтобы он был красивым. 
        //То есть был в виде n*10^k
        var power = Math.max(0,AxisNumber.log10(range));
        var previosK = power;
        var n = 0;
        var k = 0;
        while (true) {
            var base = Math.pow(10, power);
            if(base == 0) break;
            var bestNumber = AxisNumber.TICK_STEP[0];
            var minError = Double.MAX_VALUE;
            for (var multiplier : AxisNumber.TICK_STEP) {
                var tick = base * multiplier;
                if (tick <= 0) continue;
                var error = Math.abs(tick - tickInterval);
                if (error < minError) {
                    minError = error;
                    bestNumber = multiplier;
                }
            }
            var pc = power;
            if(bestNumber == AxisNumber.TICK_STEP[0]){
                if(previosK >= power && power > 0)
                    --power;
                 else {
                     var nb = Math.pow(10, power-1);
                     var tick = nb * 5;
                     if(power > 0 && tick > 0 && Math.abs(tick - tickInterval) < minError){
                         n = 5;
                         k = power-1;
                         break;
                     } else {
                         n = bestNumber;
                         k = power;
                         break;
                     }
                 }
            } else if(bestNumber == AxisNumber.TICK_STEP[AxisNumber.TICK_STEP.length - 1]){
                if(previosK <= power)
                    ++power;
                 else {
                     var nb = Math.pow(10, power+1);
                     var tick = nb * 1;
                     if(tick > 0 && Math.abs(tick - tickInterval) < minError){
                         n = 1;
                         k = power+1;
                         break;
                     } else {
                         n = bestNumber;
                         k = power;
                         break;
                     }
                 }
            } else {
                n = bestNumber;
                k = power;
                break;
            }
            previosK = pc;
        }
        var idealTickInterval = n * Math.pow(10, k);
        var min = Math.ceil(minimum/idealTickInterval)*idealTickInterval;
        var max = Math.floor(maximum/idealTickInterval)*idealTickInterval;
        {
            var hr = height/range;
            printer.setY(() -> {
                for(var i = min; i < max; i += idealTickInterval){
                    printer.tick(height-(i-minimum)*hr,format(i));
                }
            }, v -> height-(v-minimum)*hr, v -> format((height-v)/hr+minimum));
        }
        return getMaxWidth(g2d);
    }
    @Override
    public void printHorizontalTicks(Graphics2D g2d, int width, Printer printer){
        if(maximum == minimum){
            printer.setX(format(maximum), width);
            return;
        }
        var range = maximum - minimum;
        //А вот тут сложнее искать интервал
        //То есть был в виде n*10^k
        var power = Math.max(0,AxisNumber.log10(range));
        var previosK = power;
        var n = 0;
        var k = 0;
        var maxW = getMaxWidth(g2d);
        var calculateW = (java.util.function.BiFunction<Double,Integer,Integer>)(interval,pw) -> {
            var dels = (int)(range/interval); //Количество делений, которые будут размещены на оси
            if(dels == 0)return 0;
            var maxWidth = width/(dels * 2); //Расстояние между штрихами надо оставить двойное. Это очень важно, чтобы цифры не сливались!
            if(maxWidth < maxW) return width; //Чтобы показать, что двигаться надо в эту сторону
            else return maxWidth;
        };
        while (true) {
            var base = Math.pow(10, power);
            var bestNumber = AxisNumber.TICK_STEP[0];
            var minError = Double.MAX_VALUE;
            for (var multiplier : AxisNumber.TICK_STEP) {
                var tick = base * multiplier;
                if (tick <= 0) continue;
                var fwidth = calculateW.apply(tick, power);
                if(fwidth > 0 && fwidth <= minError){
                    minError = fwidth;
                    bestNumber = multiplier;
                }
            }
            var pc = power;
            if(bestNumber == AxisNumber.TICK_STEP[0]){
                if(previosK >= power && power > 0)
                    --power;
                 else {
                     var nb = Math.pow(10, power-1);
                     var tick = nb * 5;
                     var nextW = calculateW.apply(tick, power-1);
                     if(power > 0 && tick > 0 && nextW > 0 && nextW < minError){
                         n = 5;
                         k = power-1;
                         break;
                     } else {
                         n = bestNumber;
                         k = power;
                         break;
                     }
                 }
            }else if(bestNumber == AxisNumber.TICK_STEP[AxisNumber.TICK_STEP.length - 1]){
                if(previosK <= power)
                    ++power;
                 else {
                     var nb = Math.pow(10, power+1);
                     var tick = nb * 1;
                     var nextW = calculateW.apply(tick, power+1);
                     if(tick > 0 && nextW > 0 && nextW < minError){
                         n = 1;
                         k = power+1;
                         break;
                     } else {
                         n = bestNumber;
                         k = power;
                         break;
                     }
                 }
            }else {
                n = bestNumber;
                k = power;
                break;
            }
            previosK = pc;
        }
        var idealTickInterval = n * Math.pow(10, k);
        var min = Math.ceil(minimum/idealTickInterval)*idealTickInterval;
        var max = Math.floor(maximum/idealTickInterval)*idealTickInterval;
        {
            var wr = width/range;
            printer.setX(() -> {
                for(var i = min; i < max; i += idealTickInterval){
                    printer.tick((i-minimum)*wr,format(i));
                }
            }, v -> (v - minimum)*wr, v -> format(v/wr+minimum), maxW);
        }
    }

    private int getMaxWidth(Graphics2D g2d){
        var ret = 0;
        for(var v : values)
            ret = Math.max(ret,tools.getTextWidth(g2d, toString.apply(v)));
        return ret;
    }
    ///Преобразует индекс элемента в текст
    private String format(double index){
        var find = (int) index;
        if(find < 0) return "Н/Д";
        for(var i = values.iterator(); i.hasNext();--find,i.next()){
            if(find == 0)
                return toString.apply(i.next());
        }
        return "Н/Д";
    }
    ///Значения, доступные для оси
    private Collection<T> values;
    ///Функция преобразования элемента в строку
    private java.util.function.Function<T,String> toString;
}
