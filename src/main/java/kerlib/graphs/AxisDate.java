package kerlib.graphs;

import java.awt.Graphics2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kerlib.draw.tools;
import kerlib.draw.tools.alignmentX;
import kerlib.draw.tools.alignmentY;

public class AxisDate extends Axis<Date>{
    ///Часть времени, некоторая
    private static enum DatePath {
        MILISECOND(1,"SSS","."),
        SECOND(MILISECOND.length * 1000,"ss",":"),
        MINUTE(SECOND.length * 60,"mm",":"),
        HOUR(MINUTE.length * 60,"HH"," "),
        DAY(HOUR.length * 24,"dd","-"),
        MONTH(DAY.length * 30,"MM","-"),
        YEAR(Math.round(DAY.length * 365.25),"YYYY",""),
        ;
        ///Форматированная строка для вывода числа
        public static DatePath[] format = new DatePath[]{YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILISECOND};
        ///Сколько милисекунд занимает этот период
        public final long length;
        ///Формат для вывода
        public final String letters;
        ///Знак разделения разрядов
        public final String separator;

        private DatePath(long length, String letters, String separator) {
            this.length = length;
            this.letters = letters;
            this.separator = separator;
        }
        
       
        ///Возвращает интервал времени, больший, чем заданное число милисекунд
        static DatePath upper(long time){
            for(var p : values()){
                if(p.length >= time)
                    return p;
            }
            return YEAR;
        }
        ///Возваращает интервал времени, меньший, чем заданное число милисекунд
        static DatePath lower(long time){
            var values = values();
            for(var i = values.length-1; i >= 0; --i){
                if(values[i].length <= time){
                    return values[i];
                }
            }
            return MILISECOND;
        }
        ///@return возвращает предыдущее значение, меньшее, чем текущее
        DatePath previos(){return values()[Math.max(0, this.ordinal() - 1)];}
        ///@return возвращает следующее значение, большее, чем текущее
        DatePath next(){return values()[Math.min(values().length-1, this.ordinal() + 1)];}

        @Override
        public String toString(){return letters;}
    }
    
    
    public AxisDate(String name) {this(name, "");}
    public AxisDate(String name, String unit) {
        super(name, unit);
    }
    @Override
    protected double transformLocal(Date v) {
        return v.getTime();
    }
    
    @Override
    public int maxWidth(Graphics2D g2d, int height,Printer printer){
        var charWidth = tools.getTextHeight(g2d, "А");
        if(maximum == minimum || charWidth * 2 <= height){
            var formatter = new SimpleDateFormat(getNumberFormat(DatePath.YEAR,DatePath.MILISECOND));
            var text = formatter.format((long)maximum);
            printer.setY(() -> printer.tick(height/2d,text,alignmentY.center), _ -> height/2d, _ -> text);
            return tools.getTextWidth(g2d, text);
        }
        //Теперь мы знаем, на сколько делений максимум мы можем поделить нашу ось
        var range = maximum - minimum;
        //Но надо найти такой шаг между делениями, чтобы он был красивым. 
        //То есть был в виде n*10^k
        var left = DatePath.lower(Math.round(range));
        var right = left;
        var previosK = right;
        var n = 0;
        var k = DatePath.MILISECOND;
        WH: while (true) {
            var step = 1;
            var findError = 0d;
            for (var testStep : new int[]{1, 2, 5}) {
                var ticks = range / (testStep * right.length);
                if (ticks >= findError && ticks * charWidth < height) {
                    findError = ticks;
                    step = testStep;
                }
            }
            var pc = right;
            switch (step) {
                case 1 -> {
                    if(previosK.length >= right.length)
                       right = right.previos();
                    else {
                        var nb = range / (5 * right.previos().length);
                        if(nb >= findError && nb * charWidth < height){
                            n = 5;
                            k = right.previos();
                            break WH;
                        } else {
                            n = step;
                            k = right;
                            break WH;
                        }
                    }
                }
                case 2 -> {
                    n = step;
                    k = right;
                    break WH;
                }
                case 5 -> {
                    if(previosK.length <= right.length)
                       right = right.next();
                    else {
                        var nb = range / (1 * right.next().length);
                        if(nb >= findError && nb * charWidth < height){
                            n = 1;
                            k = right.next();
                            break WH;
                        } else {
                            n = step;
                            k = right;
                            break WH;
                        }
                    }
                }
            }
            previosK = pc;
        }
        var formatter = new SimpleDateFormat(getNumberFormat(left,k));
        {
            var hr = height/range;
            var step = k.length * n;
            var min = roundMin(k);
            printer.setY(() -> {
                for(var i = min; i < maximum; i += step){
                    printer.tick(height-(i-minimum)*hr,formatter.format(i));
                }
            }, v -> height-(v-minimum)*hr, v -> formatter.format(Math.round((height-v)/hr+minimum)));
        }
        return tools.getTextWidth(g2d, formatter.format((long)maximum));
    }
    @Override
    public void printHorizontalTicks(Graphics2D g2d, int width, Printer printer){
        var testDate = (long)maximum;
        if(maximum == minimum){
            var formatter = new SimpleDateFormat(getNumberFormat(DatePath.YEAR,DatePath.MILISECOND));
            var text = formatter.format(testDate);
            printer.setX(() -> printer.tick(width/2,text,alignmentX.center), _ -> width/2d, _ -> text, 0);
            return;
        }
        var range = maximum - minimum;
        //А вот тут сложнее искать интервал
        //То есть был в виде n*10^k
        var left = DatePath.lower(Math.round(range));
        var right = left;
        var previosK = right;
        var n = 0;
        var k = right;
        var calculateW = (java.util.function.BiFunction<Double,DatePath,Integer>)(dels,pw) -> {
            if(dels == 0)return 0;
            var formatter = new SimpleDateFormat(getNumberFormat(left,pw));
            var w = kerlib.draw.tools.getTextWidth(g2d, formatter.format(testDate));
            var maxWidth = width/(dels * 2); //Расстояние между штрихами надо оставить двойное. Это очень важно, чтобы цифры не сливались!
            if(maxWidth < w) return width; //Чтобы показать, что двигаться надо в эту сторону
            else return (int)maxWidth;
        };
        WH: while (true) {
            var bestNumber = 1;
            var findError = Double.MAX_VALUE;
            for (var testStep : new int[]{1, 2, 5}) {
                var ticks = range / (testStep * right.length);
                var fwidth = calculateW.apply(ticks, right);
                if(fwidth > 0 && fwidth <= findError){
                    findError = fwidth;
                    bestNumber = testStep;
                }
            }
            var pc = right;
            switch (bestNumber) {
                case 1 -> {
                    if(previosK.length >= right.length)
                       right = right.previos();
                    else {
                        var ticks = range / (5 * right.previos().length);
                        var nextW = calculateW.apply(ticks, right.previos());
                        if(nextW > 0 && nextW < findError){
                            n = 5;
                            k = right.previos();
                            break WH;
                        } else {
                            n = bestNumber;
                            k = right;
                            break WH;
                        }
                    }
                }
                case 2 -> {
                    n = bestNumber;
                    k = right;
                    break WH;
                }
                case 5 -> {
                    if(previosK.length <= right.length)
                       right = right.next();
                    else {
                        var ticks = range / (5 * right.next().length);
                        var nextW = calculateW.apply(ticks, right.next());
                        if(nextW > 0 && nextW < findError){
                            n = 1;
                            k = right.next();
                            break WH;
                        } else {
                            n = bestNumber;
                            k = right;
                            break WH;
                        }
                    }
                }
            }
            previosK = pc;
        }
        var formatter = new SimpleDateFormat(getNumberFormat(left,k));
        {
            var wr = width/range;
            var step = k.length * n;
            var min = roundMin(k);
            printer.setX(() -> {
                for(var i = min; i < maximum; i += step){
                    printer.tick((i-minimum)*wr,formatter.format(i));
                }
            }, v -> (v - minimum)*wr, v -> formatter.format(v/wr+minimum), kerlib.draw.tools.getTextWidth(g2d, formatter.format(testDate)));
        }
    }
    /**@return минимальное значение, которое будет отображено на оси при заданном интервале */
    private long roundMin(DatePath val){
        switch (val) {
            case MILISECOND -> {
                return Math.round(minimum);
            }
            case SECOND -> {
                return Math.round(Math.ceil(minimum/1000)*1000);
            }
            case MINUTE -> {
                return Math.round(Math.ceil(minimum/(60 * 1000))*(60 * 1000));
            }
            case HOUR -> {
                return Math.round(Math.ceil(minimum/(60 * 60 * 1000))*(60 * 60 * 1000));
            }
            case DAY -> {
                var calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Math.round(minimum));
                var isNext = calendar.get(Calendar.MILLISECOND) > 0 || calendar.get(Calendar.SECOND) > 0 || calendar.get(Calendar.MINUTE) > 0 || calendar.get(Calendar.HOUR_OF_DAY) > 0;
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                if(isNext)
                    calendar.set(Calendar.DAY_OF_MONTH, calendar.get(Calendar.DAY_OF_MONTH)+1);
                return calendar.getTime().getTime();
            }
            case MONTH -> {
                var calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Math.round(minimum));
                var isNext = calendar.get(Calendar.MILLISECOND) > 0 || calendar.get(Calendar.SECOND) > 0 || calendar.get(Calendar.MINUTE) > 0 || calendar.get(Calendar.HOUR_OF_DAY) > 0 || calendar.get(Calendar.DAY_OF_MONTH) > 1;
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                if(isNext)
                    calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH)+1);
                return calendar.getTime().getTime();
            }
            case YEAR -> {
                var calendar = Calendar.getInstance();
                calendar.setTimeInMillis(Math.round(minimum));
                var isNext = calendar.get(Calendar.MILLISECOND) > 0 || calendar.get(Calendar.SECOND) > 0 || calendar.get(Calendar.MINUTE) > 0 || calendar.get(Calendar.HOUR_OF_DAY) > 0 || calendar.get(Calendar.DAY_OF_YEAR) > 1;
                calendar.set(Calendar.MILLISECOND, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.DAY_OF_YEAR, 1);
                if(isNext)
                    calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR)+1);
                return calendar.getTime().getTime();
            }
            default -> throw new IllegalArgumentException("Неизвестный тип округления " + val);
        }
    }
    ///Возвращает форматирование для даты
    /// @param left левая граница (макисмальная. То есть время по неё обрезается)
    /// @param right правая граница (минимальная)
    private static String getNumberFormat(DatePath left, DatePath right) {
        var sb = new StringBuffer();
        for(var f : DatePath.format){
            if(right.ordinal() <= f.ordinal() && f.ordinal() <= left.ordinal()){
                if(!sb.isEmpty())
                    sb.append(f.separator);
                sb.append(f);
            }
        }
        if(left != right && right == DatePath.HOUR)return sb.append(":00").toString();
        else return sb.toString();
    }

    @Override
    public String toString() {
        return String.format("%s%f;%f%s,%s%s", isAutoresizeMin ? "<-" : "[", minimum, maximum, isAutoresizeMax ? "->" : "]", name, unit);
    }
}
