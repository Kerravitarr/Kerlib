package kerlib.graphs;

import java.awt.Graphics2D;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import kerlib.draw.tools;
public class AxisDate extends Axis<Date>{
    ///Часть времени, некоторая
    private static enum DatePath {
        MILISECOND(1,"SSS",".",Calendar.MILLISECOND,0, new int[]{1,2,5,10,20,50,100,200,500}),
        SECOND(MILISECOND.length * 1000,"ss",":",Calendar.SECOND,0,new int[]{1,2,5,10,20,30}),
        MINUTE(SECOND.length * 60,"mm",":", Calendar.MINUTE,0,new int[]{1,2,5,10,20,30}),
        HOUR(MINUTE.length * 60,"HH"," ",Calendar.HOUR_OF_DAY,0,new int[]{1,2,3,6,12}),
        DAY(HOUR.length * 24,"dd","-",Calendar.DAY_OF_MONTH,1,new int[]{1,2,7}),
        MONTH(DAY.length * 30,"MM","-",Calendar.MONTH,1,new int[]{1,2,3,4,6}),
        YEAR(Math.round(DAY.length * 365.25),"YYYY","",Calendar.YEAR,0,new int[]{1,2,5,10,20,50,100,200,500,1000,2000,5000,10_000,20_000,50_000}),
        ;
        ///Форматированная строка для вывода числа
        public static DatePath[] format = new DatePath[]{YEAR, MONTH, DAY, HOUR, MINUTE, SECOND, MILISECOND};
        ///Сколько милисекунд занимает этот период
        public final long length;
        ///Формат для вывода
        public final String letters;
        ///Знак разделения разрядов
        public final String separator;
        ///Эквивалент объекта календаря
        public final int calendarEqual;
        ///Минимальное значение этого шага
        public final int minValue;
        ///С каким шагом изменяем время в этом интервале
        public final int[] NUMBER_STEP;

        private DatePath(long length, String letters, String separator, int calendar,int min, int[] steps) {
            this.length = length;
            this.letters = letters;
            this.separator = separator;
            this.minValue = min;
            this.calendarEqual = calendar;
            this.NUMBER_STEP = steps;
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
            return printer.setY(MAX_FORMAT.format((long)maximum), height);
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
        while (true) {
            var steps = right.NUMBER_STEP;
            var start = steps[0];
            var end = steps[steps.length-1];
            var step = start;
            var findError = 0d;
            for (var testStep : steps) {
                var ticks = range / (testStep * right.length);
                if (ticks >= findError && ticks * charWidth < height) {
                    findError = ticks;
                    step = testStep;
                }
            }
            var pc = right;
            if(step == start){
                if(previosK.length >= right.length)
                    right = right.previos();
                else {
                    var nb = range / (end * right.previos().length);
                    if(nb >= findError && nb * charWidth < height){
                        n = end;
                        k = right.previos();
                        break;
                    } else {
                        n = step;
                        k = right;
                        break;
                    }
                }
            } else if(step == end){
                if(previosK.length <= right.length)
                    right = right.next();
                else {
                    var nb = range / (start * right.next().length);
                    if(nb >= findError && nb * charWidth < height){
                        n = start;
                        k = right.next();
                        break;
                    } else {
                        n = step;
                        k = right;
                        break;
                    }
                }
            } else {
                n = step;
                k = right;
                break;
            }
            previosK = pc;
        }
        var formatter = new SimpleDateFormat(getNumberFormat(left,k));
        {
            var hr = height/range;
            var stepType = k.calendarEqual;
            var stepSize = n;
            var min = roundMin(k,stepSize);
            printer.setY(() -> {
                for(var i = min.getTime().getTime(); i < maximum;){
                    printer.tick(height-(i-minimum)*hr,formatter.format(i));
                    min.set(stepType, min.get(stepType)+stepSize);
                    i = min.getTime().getTime();
                }
            }, v -> height-(v-minimum)*hr, v -> formatter.format(Math.round((height-v)/hr+minimum)));
        }
        return tools.getTextWidth(g2d, formatter.format((long)maximum));
    }
    @Override
    public void printHorizontalTicks(Graphics2D g2d, int width, Printer printer){
        var testDate = (long)maximum;
        if(maximum == minimum){
            printer.setX(MAX_FORMAT.format(testDate), width);
            return;
        }
        var range = maximum - minimum;
        //А вот тут сложнее искать интервал. Потому что для разных чисел - он плавающий!
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
        while (true) {
            var steps = right.NUMBER_STEP;
            var start = steps[0];
            var end = steps[steps.length-1];
            var step = start;
            var findError = Double.MAX_VALUE;
            for (var testStep : steps) {
                var ticks = range / (testStep * right.length);
                var fwidth = calculateW.apply(ticks, right);
                if(fwidth > 0 && fwidth <= findError){
                    findError = fwidth;
                    step = testStep;
                }
            }
            var pc = right;
            if(step == start){
                if(previosK.length >= right.length && right != DatePath.MILISECOND)
                    right = right.previos();
                else {
                    var ticks = range / (end * right.previos().length);
                    var nextW = calculateW.apply(ticks, right.previos());
                    if(nextW > 0 && nextW < findError){
                        n = end;
                        k = right.previos();
                        break;
                    } else {
                        n = step;
                        k = right;
                        break;
                    }
                }
            } else if(step == end){
                if(previosK.length <= right.length && right != DatePath.YEAR)
                    right = right.next();
                else {
                    var ticks = range / (start * right.next().length);
                    var nextW = calculateW.apply(ticks, right.next());
                    if(nextW > 0 && nextW < findError){
                        n = start;
                        k = right.next();
                        break;
                    } else {
                        n = step;
                        k = right;
                        break;
                    }
                }
            } else {
                n = step;
                k = right;
                break;
            }
            previosK = pc;
        }
        var formatter = new SimpleDateFormat(getNumberFormat(left,k));
        {
            var wr = width/range;
            var stepType = k.calendarEqual;
            var stepSize = n;
            var min = roundMin(k,stepSize);
            printer.setX(() -> {
                for(var i = min.getTime().getTime(); i < maximum;){
                    printer.tick((i-minimum)*wr,formatter.format(i));
                    min.set(stepType, min.get(stepType)+stepSize);
                    i = min.getTime().getTime();
                }
            }, v -> (v - minimum)*wr, v -> formatter.format(v/wr+minimum), kerlib.draw.tools.getTextWidth(g2d, formatter.format(testDate)));
        }
    }
    /**@return минимальное значение, которое будет отображено на оси при заданном интервале */
    private Calendar roundMin(DatePath val, int step){
        var calendar = Calendar.getInstance();
        calendar.setTimeInMillis(Math.round(minimum));
        var isNext = false;
        for(var c : DatePath.values()){
            if(c == val) break;
            isNext |= calendar.get(c.calendarEqual) > 0;
            calendar.set(c.calendarEqual, c.minValue);
        }
        if(isNext){
            switch (val) {
                case MILISECOND,SECOND,MINUTE,HOUR -> {
                    var next = calendar.get(val.calendarEqual) + 1;
                    var ost = next % step;
                    calendar.set(val.calendarEqual, ost == 0 ? next : (next + step - ost));
                }
                case DAY ->{
                    if(step == 7){
                        ///Если шаг по 7 дней, то пусть будет по понедельникам шаг
                        var tar = calendar.get(val.calendarEqual) + 1;
                        calendar.set(val.calendarEqual, tar);
                        if(calendar.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY){
                            calendar.set(val.calendarEqual, tar + (7 - (calendar.get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY)));
                        }
                    } else {
                        calendar.set(val.calendarEqual, calendar.get(val.calendarEqual) + 1);
                    }
                }
                case MONTH -> {
                    //Месяц просто начинается с 1, а не с 0
                    if(step == 3){
                        //По 3 месяца - по сезонам!
                        var tar = calendar.get(val.calendarEqual) + 1;
                        calendar.set(val.calendarEqual, tar);
                        var newM = calendar.get(val.calendarEqual);
                        switch (newM) {
                            case Calendar.JANUARY,Calendar.APRIL,Calendar.JULY,Calendar.OCTOBER -> {
                                calendar.set(val.calendarEqual, tar+2);
                            }
                            case Calendar.FEBRUARY,Calendar.MAY,Calendar.AUGUST,Calendar.NOVEMBER -> {
                                calendar.set(val.calendarEqual, tar+1);
                            }
                            default -> throw new AssertionError();
                        }
                    } else {
                        var next = calendar.get(val.calendarEqual) + 1;
                        var ost = next % step;
                        calendar.set(val.calendarEqual, next + (ost == 0 ? 0 : (step - ost)));
                    }
                }
                case YEAR -> {
                    calendar.set(val.calendarEqual, calendar.get(val.calendarEqual) + 1);
                }
            }
        }
        return calendar;
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
    ///Формат для вывода максимального значения числа
    private final static SimpleDateFormat MAX_FORMAT = new SimpleDateFormat(getNumberFormat(DatePath.YEAR,DatePath.MILISECOND));
}
