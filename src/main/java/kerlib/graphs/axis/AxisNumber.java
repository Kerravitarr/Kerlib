package kerlib.graphs.axis;

import java.awt.Graphics2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

import kerlib.draw.tools;
import kerlib.graphs.Axis;

///Ось для списка любых чисел в любых размерностях
/// 
/// @author Kerravitarr (github.com/Kerravitarr)
public class AxisNumber<T extends Number> extends Axis<T>{
    ///Формат вывода чисел
    private final DecimalFormatSymbols SYMBOLS;
    ///Коэффициенты округления чисел
    final static int[] TICK_STEP = new int[]{1, 2, 5};
    ///Масштаб оси. Масштаб входных данных
    private double scale = 1;
    
    
    public AxisNumber(String name) {this(name, "");}
    public AxisNumber(String name, String unit) {
        super(name, unit);

        Locale locale = Locale.getDefault();
        SYMBOLS = new DecimalFormatSymbols(locale);
    }
    ///@param groupingSeparator задаёт символ, используемый в качестве разделителя групп
    public void setGroupingSeparator(char groupingSeparator){
        getFormatSymbols().setGroupingSeparator(groupingSeparator);
    }
    ///@return Элемент формарматирвоания числа
    public DecimalFormatSymbols getFormatSymbols(){return SYMBOLS;}
    
    public void scale(double scale){
        if(this.scale != scale){
            this.scale = scale;
            reset();
            fireChangeEvent(Axis.AxisUpdateEvent.STATUS.NEED_RECALCULATE);
        }
    }
    @Override
    protected double transformLocal(T v) {
        return v.doubleValue() * scale;
    }
    
    @Override
    public int maxWidth(Graphics2D g2d, int height,Printer printer){
        var charWidth = tools.getTextHeight(g2d, "А");
        var previous = Math.floor(height / (charWidth*2));
        if(maximum == minimum || previous <= 1){
            return printer.setY(getNumberFormat(log10(maximum)).format(maximum),height);
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
        while (true) {
            var base = Math.pow(10, power);
            if(base == 0) break;
            var bestNumber = TICK_STEP[0];
            var minError = Double.MAX_VALUE;
            for (var multiplier : TICK_STEP) {
                var tick = base * multiplier;
                if (tick <= 0) continue;
                var error = Math.abs(tick - tickInterval);
                if (error < minError) {
                    minError = error;
                    bestNumber = multiplier;
                }
            }
            var pc = power;
            if(bestNumber == TICK_STEP[0]){
                if(previosK >= power)
                    --power;
                 else {
                     var nb = Math.pow(10, power-1);
                     var tick = nb * 5;
                     if(tick > 0 && Math.abs(tick - tickInterval) < minError){
                         n = 5;
                         k = power-1;
                         break;
                     } else {
                         n = bestNumber;
                         k = power;
                         break;
                     }
                 }
            } else if(bestNumber == TICK_STEP[TICK_STEP.length - 1]){
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
        var min = roundMin(idealTickInterval);
        var max = roundMax(idealTickInterval);
        var formatter = getNumberFormat(k);
        {
            var hr = height/range;
            printer.setY(() -> {
                var prevI = min;
                for(var i = min; i < max; ){
                    printer.tick(height-(i-minimum)*hr,formatter.format(i));
                    
                    if ((i += idealTickInterval) == prevI) {
                        if (idealTickInterval > 0) i = Math.nextUp(i);
                        else i = Math.nextDown(i);
                    }
                    prevI = i;
                }
            }, v -> height-(v-minimum)*hr, v -> formatter.format((height-v)/hr+minimum));
        }
        return Math.max(tools.getTextWidth(g2d, formatter.format(min)),tools.getTextWidth(g2d, formatter.format(max)));
    }    
    @Override
    public void printHorizontalTicks(Graphics2D g2d, int width, Printer printer){
        if(maximum == minimum){
            printer.setX(getNumberFormat(log10(maximum)).format(maximum), width);
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
            var formatter = getNumberFormat(pw);
            var w = kerlib.draw.tools.getTextWidth(g2d, formatter.format(max));
            var maxWidth = width/(dels * 2); //Расстояние между штрихами надо оставить двойное. Это очень важно, чтобы цифры не сливались!
            if(maxWidth < w) return width; //Чтобы показать, что двигаться надо в эту сторону
            else return maxWidth;
        };
        while (true) {
            var base = Math.pow(10, power);
            var bestNumber = TICK_STEP[0];
            var minError = Double.MAX_VALUE;
            for (var multiplier : TICK_STEP) {
                var tick = base * multiplier;
                if (tick <= 0) continue;
                var fwidth = calculateW.apply(tick, power);
                if(fwidth > 0 && fwidth <= minError){
                    minError = fwidth;
                    bestNumber = multiplier;
                }
            }
            var pc = power;
            if(bestNumber == TICK_STEP[0]){
                if(previosK >= power)
                    --power;
                 else {
                     var nb = Math.pow(10, power-1);
                     var tick = nb * 5;
                     var nextW = calculateW.apply(tick, power-1);
                     if(tick > 0 && nextW > 0 && nextW < minError){
                         n = 5;
                         k = power-1;
                         break;
                     } else {
                         n = bestNumber;
                         k = power;
                         break;
                     }
                 }
            }else if(bestNumber == TICK_STEP[TICK_STEP.length - 1]){
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
        var min = roundMin(idealTickInterval);
        var max = roundMax(idealTickInterval);
        var formatter = getNumberFormat(k);
        {
            var wr = width/range;
            printer.setX(() -> {
                var prevI = min;
                
                for(var i = min; i < max;){
                    printer.tick((i-minimum)*wr,formatter.format(i));
                    
                    if ((i += idealTickInterval) == prevI) {
                        if (idealTickInterval > 0) i = Math.nextUp(i);
                        else i = Math.nextDown(i);
                    }
                    prevI = i;
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
    
    private DecimalFormat getNumberFormat(int k) {
        if (k < 0) {
            return new DecimalFormat("#,##0." + "0".repeat(-k), getFormatSymbols());
        } else {
            return new DecimalFormat("#,###", getFormatSymbols()); // Без десятичных знаков
        }
    }
    static int log10(double val){
        return val == 0 ? 1 : (int) Math.floor(Math.log10(val));
    }

    @Override
    public String toString() {
        return String.format("%s%f;%f%s,%s%s", isAutoresizeMin ? "<-" : "[", minimum, maximum, isAutoresizeMax ? "->" : "]", name, unit);
    }
}
