package kerlib;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;

/**
 *Буфер сжатия. Его основная фишка в работе, постараюсь объяснить:
 * Шаг 1: Пришла 1 и заняла своё место {1,0}→1 [1,0]. В буфере 2 пока добавляется сумма {1,0}→1 [1,0]
Шаг 2: Пришла 2 и сдвинула 1 на следующее место {2,1}→3 [3,0]. В буфере 2 обновляется сумма {3,0}→3 [3,0]
Шаг 3: Пришла 3. В буфере уже два числа 2 и 1, поэтому их сумма 3 переходит дальше, а буфер сдвигается и записывается 3 {3,2}→5 [5,3]. В буфере 2 сумма переходит дальше, а в начало встаёт новая сумма {5,3}→8 [8,0]
Шаг 4: Пришла 4. 4 опять записывается в первую ячейку {4,3}→7 [7,3]. В буфере 2 пока меняется только первое число {7,3}→10 [10,0]
Шаг 5: Пришла 5. Обновляем значения и записываем 5 в своё место {5,4}→9 [9,7]. В буфере 2 тоже сдвигаем данные  {9,7}→16 [16,10]
* Таким образом, набирая эти буферы в цепочку, в последнем буффере всегда будут все значения, при этом каждый следующий буффер в масштабе меньше предыдущего
* Однако есть и свой минус: Первый элемент массива у нас всегда "опережает" своё значение. Так что его лучше не использовать для всяких сложных оценок
* Ну и нужен для того, чтобы формировать оценки с разной периодичностью. Первый буфер в цепочке повторяет входящие значения, а последний
* ели-ели движется, позволяя хранить данные с разным масштабом
 * @author Илья
 * @param <T> Тип значений, которые храняться в буффере
 */
public class ZipBuffer<T extends Number>{
    ///@return Бесконечный буфер, который будет хранить лишь один элемент, как сумма всех элементов
    public static ZipBuffer<Integer> asIn(){return asIn(0);}
    /// @param size количество элементов в этом буфере. 
    /// Если 0, то буфер будет бесконечно расти и никогда не передаст значение дальше,
    ///  Если меньше нуля - буфер будет только с одним элементом, при этом дальше значения будут передаваться, будто этого буффера не существует
    /// @return Не сжимающий буфер, по факту обычный кольцевой буфер заданного размера
    public static ZipBuffer<Integer> asIn(int size){return asIn(size, 0);}
    /// @param size количество элементов в этом буфере. 
    /// Если 0, то буфер будет бесконечно расти и никогда не передаст значение дальше,
    ///  Если меньше нуля - буфер будет только с одним элементом, при этом дальше значения будут передаваться, будто этого буффера не существует
    /// @param zip на сколько сжимать входящие значения. Если тут будет число 5, а size = 100, то
    ///      степень сжатия определяется как 100/5= 20 раз. 
    /// @return Сжимающий буфер
    public static ZipBuffer<Integer> asIn(int size, int zip){return new ZipBuffer<>(Integer.class, size, zip);}
    /// @return Буфер из одного элемента, который хранит просто сумму по входному значению и передаёт входное значение дальше
    public static ZipBuffer<Integer> asCounterIn(){return asIn(-1);}
    ///@return Бесконечный буфер, который будет хранить лишь один элемент, как сумма всех элементов
    public static ZipBuffer<Double> asDouble(){return asDouble(0);}
    /// @param size количество элементов в этом буфере. 
    /// Если 0, то буфер будет бесконечно расти и никогда не передаст значение дальше,
    ///  Если меньше нуля - буфер будет только с одним элементом, при этом дальше значения будут передаваться, будто этого буффера не существует
    /// @return Не сжимающий буфер, по факту обычный кольцевой буфер заданного размера
    public static ZipBuffer<Double> asDouble(int size){return asDouble(size, 0);}
    /// @param size количество элементов в этом буфере. 
    /// Если 0, то буфер будет бесконечно расти и никогда не передаст значение дальше,
    ///  Если меньше нуля - буфер будет только с одним элементом, при этом дальше значения будут передаваться, будто этого буффера не существует
    /// @param zip на сколько сжимать входящие значения. Если тут будет число 5, а size = 100, то
    ///      степень сжатия определяется как 100/5= 20 раз. 
    /// @return Сжимающий буфер
    public static ZipBuffer<Double> asDouble(int size, int zip){return new ZipBuffer<>(Double.class, size, zip);}

    /**Большое кольцо, которое хранит значения*/
    private final ArrayDeque<T> bigRing;
    /**Малое кольцо, которое хранит значения только для малой суммы*/
    private final ArrayDeque<T> miniRing;
    /**Размер буфера*/
    private final int size;
    /**На сколько надо сжимать входные значения*/
    private final int zip;
    ///Следующий буфер в цепочке
    private ZipBuffer<T> next;
    ///Предыдущий буфер в цепочке. Нужен, чтобы мы могли узнать коэффициент сжатия данных 
    private ZipBuffer<T> previos;
    /**Бесконечный буфер?*/
    private final boolean isInfinity;
    /**Один элемент в виде суммы?*/
    private final boolean isCounter;
    ///Функция нахождения суммы элементов
    private final java.util.function.BiFunction<T,T,T> sum;
    ///Функция нахождения разности элементов
    private final java.util.function.BiFunction<T,T,T> difference;
    
    /**Сколько элементов в малом кольце*/
    private long minRingSize = 0;
    /**Сумма элементов в малом кольце*/
    private T minRingSum;
    
    /**Создаёт элемент буффера
     * @param clazz класс, который является основным для этого буфера
     * @param size количество элементов в этом буфере. 
     *  Если 0, то буфер будет бесконечно расти и никогда не передаст значение дальше,
     *  Если меньше нуля - буфер будет только с одним элементом, при этом дальше значения будут передаваться, будто этого буффера не существует
     * @param zip на сколько сжимать входящие значения. Если тут будет число 5, а size = 100, то
     *      степень сжатия определяется как 100/5= 20 раз. 
     *      В реальности это определяет, сколько буфер будет ждать значений, прежде чем увеличить своё реальное значение.
     *      То есть при 5 буфер будет ждат ь 5 значений, найдёт их сумму и изменит первое своё значение
     */
    public ZipBuffer(Class<T> clazz, int size, int zip){
        isInfinity = size == 0;
        isCounter = size < 0;
        this.zip = isCounter ? 1 : Math.max(1,zip);
        this.size = Math.max(1,size);
        bigRing = new ArrayDeque<>(this.size);
        miniRing = new ArrayDeque<>(this.zip);
        minRingSum = kerlib.tools.unbox(clazz, 0);
        for (int i = 0; i < this.size; i++) {bigRing.add(minRingSum);}
        for (int i = 0; i < this.zip; i++) {miniRing.add(minRingSum);}
        if(clazz == Double.class || clazz == double.class){
            sum = (a,b) -> (T)(Double.valueOf(a.doubleValue() + b.doubleValue()));
            difference = (a,b) -> (T)(Double.valueOf(a.doubleValue() - b.doubleValue()));
        } else if(clazz == Float.class || clazz == float.class){
            sum = (a,b) -> (T)(Float.valueOf(a.floatValue() + b.floatValue()));
            difference = (a,b) -> (T)(Float.valueOf(a.floatValue() - b.floatValue()));
        } else if(clazz == Long.class || clazz == long.class){
            sum = (a,b) -> (T)(Long.valueOf(a.longValue() + b.longValue()));
            difference = (a,b) -> (T)(Long.valueOf(a.longValue() - b.longValue()));
        } else if(clazz == Integer.class || clazz == int.class){
            sum = (a,b) -> (T)(Integer.valueOf(a.intValue() + b.intValue()));
            difference = (a,b) -> (T)(Integer.valueOf(a.intValue() - b.intValue()));
        } else if(clazz == Short.class || clazz == short.class){
            sum = (a,b) -> (T)(Short.valueOf((short)(a.shortValue() + b.shortValue())));
            difference = (a,b) -> (T)(Short.valueOf((short)(a.shortValue() - b.shortValue())));
        } else if(clazz == Byte.class || clazz == byte.class){
            sum = (a,b) -> (T)(Byte.valueOf((byte)(a.byteValue() + b.byteValue())));
            difference = (a,b) -> (T)(Byte.valueOf((byte)(a.byteValue() - b.byteValue())));
        } else {
            throw new IllegalArgumentException("Неподдерживаемый тип данных для буфера: " + clazz.getName());
        }
    }

    ///Сохраняет следующий буфер, по отношению к текущему
    /// @param next следующий буфер
    /// @return Следующий буфер, чтобы к нему можно было добавить ещё - продолжить цепочку
    public ZipBuffer<T> next(ZipBuffer<T> next){
        this.next = next;
        if(next != null)
            next.previos = this;
        return next;
    }
    /**Добавляет элемент в буфер. Добавлять следует только в последний буффер элементы, в остальные значения перейдут сами
     * @param element последний элемент выборки
     */
    public void add(T element){
        if(isCounter){
            minRingSum = sum.apply(minRingSum, element);
            bigRing.removeFirst();
            bigRing.addFirst(minRingSum);
            if(next != null) next.add(element);
        } else {
            if(minRingSize++ % zip == 0){
                //Заполнили весь подбуфер
                if(!isInfinity || minRingSize == 1)
                    bigRing.removeLast();
                bigRing.addFirst(minRingSum);
                if(next != null && !isInfinity) next.add(minRingSum);
            }
            minRingSum = sum.apply(minRingSum, difference.apply(element, miniRing.removeLast()));
            miniRing.addFirst(element);
            bigRing.removeFirst();
            bigRing.addFirst(minRingSum);
            if(!isInfinity && next != null) next.readd(minRingSum);
        }
    }
    public Iterator<T> iterator(){return bigRing.iterator();}
    public List<T> asList(){return bigRing.stream().toList();}

    @Override
    public String toString() {
        var sb = new StringBuffer();
        sb.append('[');
        if(!isCounter){
            sb.append('{');
            var isFirst = true;
            for(var val : miniRing){
                if(isFirst) isFirst = false;
                else sb.append(',');
                sb.append(val);
            }
            sb.append("}->");
        }
        var isFirst = true;
        for(var val : bigRing){
            if(isFirst) isFirst = false;
            else sb.append(',');
            sb.append(val);
        }
        sb.append("] ");
        if(next != null)
            sb.append(next);
        return sb.toString();
    }
    
    
    /**Внутренний метод, нужен, чтобы изменить значение внутреннего буфера, пока остальные значения не изменились
     * @param element 
     */
    private void readd(T element){
        minRingSum = sum.apply(minRingSum, difference.apply(element, miniRing.removeFirst()));
        miniRing.addFirst(element);
        bigRing.removeFirst();
        bigRing.addFirst(minRingSum);
        if(!isInfinity && next != null) next.readd(minRingSum);
    }
}
