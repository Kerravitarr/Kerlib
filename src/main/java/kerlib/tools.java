/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib;

import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author zeus
 */
public class tools {
    ///Объект информации о системе
    private final static java.lang.management.OperatingSystemMXBean OSMXB = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
	/**
	 * Округляет число до ближайшего целого
	 * @param d округляемое число
	 * @return ближайшее целое
	 */
	public static int round(double d) {
		return (int) Math.round(d);
	}
	/**
	 * Округляет число до нужного количества занчащих цифр
	 * @param d округляемое число
	 * @param digits сколько ЗНАЧАЩИХ цифр должно быть в числе
	 * @return ближайшее число.
	 */
	public static double round(double d, int digits) {
		if(d == 0) return 0;
		else if(d > 0){
			var pow = digits - Math.ceil(Math.log10(d));
			var m = Math.pow(10, pow);
			return Math.round(d*m)/((double)m);
		} else {
			var pow = digits - Math.ceil(Math.log10(-d));
			var m = Math.pow(10, pow);
			return -Math.round(-d*m)/((double)m);
		}
	}
    
    	/**Осуществляет преобразование из класса в класс
	* @param <T> итоговый класс
	* @param cls класс, который описывает то, к чему мы стремимся
	* @return объект, нужного типа
	* @throws ClassCastException когда не смогли преобразовать один тип к другому
	*/
   	@SuppressWarnings("unchecked")
	public static <T> T unbox(Class<T> cls, Object o) throws ClassCastException{
        if (o == null) {
            return null;
        } else if(cls.isAssignableFrom(o.getClass())){
            return (T) o;
        } else if(cls.isEnum() && o instanceof String rets){
            return (T) (Enum.valueOf((Class<Enum>) cls, rets));
        } else if(cls.equals(Byte.class) || cls.equals(byte.class)){
            return (T) Byte.valueOf(((Number)o).byteValue());
        } else if(cls.equals(Double.class) || cls.equals(double.class)){
            return (T) Double.valueOf(((Number)o).doubleValue());
        } else if(cls.equals(Float.class) || cls.equals(float.class)){
            return (T) Float.valueOf(((Number)o).floatValue());
        } else if(cls.equals(Integer.class) || cls.equals(int.class)){
            return (T) Integer.valueOf(((Number)o).intValue());
        } else if(cls.equals(Long.class) || cls.equals(long.class)){
            return (T) Long.valueOf(((Number)o).longValue());
        } else if(cls.equals(Short.class) || cls.equals(short.class)){
            return (T) Short.valueOf(((Number)o).shortValue());
        } else if(cls.equals(Boolean.class) || cls.equals(boolean.class)){
            return (T) Boolean.valueOf(((Boolean)o).booleanValue());
        } else {
            throw new ClassCastException("Невозможно привести " + o.getClass() + " к " + cls);
        }
    }
    ///Возвращает среднюю загрузку системы
    ///@return -1, если данные недоступны. Число в интервале [0,1] показывает в процентах, сколько системы загруженно.
    ///         1 - предел, когда все программы выполняются и ни кто не ждёт своей очереди
    ///         Всё, что выше 1 означает, что система перегружена. Программы ждут своей очереди на получение доступа к процессору
    public static double systemLoad(){
        var sl = OSMXB.getSystemLoadAverage();
        if(sl < 0) return -1;
        else return sl / OSMXB.getAvailableProcessors();
    }
    
    /**
	 * Приравнивает число к интервалу [min,max]
	 * @param min минимальное значение, меньше которого выходное число точно не будет
	 * @param val число, которое нужно ограничить
	 * @param max максимальное значение, больше которого выходное число точно не будет
	 * @return 
	 */
	public static double betwin(double min, double val, double max) {
        return val > max ? max : (val < min ? min : val);
	}
	/**
	 * Приравнивает число к интервалу [min,max]
	 * @param min минимальное значение, меньше которого выходное число точно не будет
	 * @param val число, которое нужно ограничить
	 * @param max максимальное значение, больше которого выходное число точно не будет
	 * @return 
	 */
	public static float betwin(float min, float val, float max) {
        return val > max ? max : (val < min ? min : val);
	}	
	/**
	 * Приравнивает число к интервалу [min,max]
	 * @param min минимальное значение, меньше которого выходное число точно не будет
	 * @param val число, которое нужно ограничить
	 * @param max максимальное значение, больше которого выходное число точно не будет
	 * @return 
	 */
	public static int betwin(int min, int val, int max) {
        return val > max ? max : (val < min ? min : val);
	}	
    ///Переменная, что надо написать о том, что остался отладочный код
    private static boolean isWriteToLogError = false;
    @FunctionalInterface public interface Worker {void something();}
    /** @param doing будет выполнена, только если включены assert (запуск с флагом -enableassertions)*/
    public static void isAssert(Worker doing){
        if(isAssert(true,false)) doing.something();
    }
    /** @param doing будет выполнена, только если НЕ включены assert (запуск без флага -enableassertions)*/
    public static void noAssert(Worker doing){
        if(isAssert(false,true)) doing.something();
    }
    ///Функция, возвращающая разное значение в зависимости от ключённого режима
    ///Нужна для отладки. Для того, чтобы у приложения был разный режим в зависимости от включённого режима предупреждений
    /// @param <T> тип
    /// @param noAssert возвращаемое значение в нормальном режиме
    /// @param isAssert возвращаемое значение, если приложение запущено с флагом -enableassertions
    /// @return флаг -enableassertions ? isAssert : noAssert
    public static <T> T isAssert(T noAssert, T isAssert){
        var isA = false;
        assert(isA = true);
        if(!isA) debugCode();
        return isA ? isAssert : noAssert;
    }
    ///Экранирует все неподходящие символы, чтобы текст корректно отображался в html
    ///@param s входная строка
    ///@return текст с экранированными символами
    public static String escape(String s) {
        var builder = new StringBuilder();
        var previousWasASpace = false;
        for( var c : s.toCharArray() ) {
            if( c == ' ' ) {
                if( previousWasASpace ) {
                    builder.append("&nbsp;");
                    previousWasASpace = false;
                    continue;
                }
                previousWasASpace = true;
            } else {
                previousWasASpace = false;
            }
            switch(c) {
                case '<' -> builder.append("&lt;");
                case '>' -> builder.append("&gt;");
                case '&' -> builder.append("&amp;");
                case '"' -> builder.append("&quot;");
                case '\n' -> builder.append("<br>");
                case '\t' -> builder.append("&nbsp; &nbsp; &nbsp;");
                default -> {
                    if( c < 128 ) builder.append(c);
                    else builder.append("&#").append((int)c).append(";");
                }
            }
        }
        return builder.toString();
    }
    /**Ищет самый молодой файл в заданном месте по дате последней модификации (тот, который позже всех редактировался)
     * @param where где искать, путь к файлу
     * @param filter функция фильтраци файлов. Должна вернуть true для подходящих файлов
     * @return самый старый файл в заданном месте или null
     */
    public static java.io.File findYoungest(String where, java.util.function.Function<java.io.File, Boolean> filter){
        return findYoungest(new java.io.File(where), filter);
    }
    /**Ищет самый молодой файл в заданном месте по дате последней модификации (тот, который позже всех редактировался)
     * @param where где искать
     * @param filter функция фильтраци файлов. Должна вернуть true для подходящих файлов
     * @return самый старый файл в заданном месте или null
     */
    public static java.io.File findYoungest(java.io.File where, java.util.function.Function<java.io.File, Boolean> filter){
        if(!where.exists()) return null;
        java.io.File old = null;
        for(var file : where.listFiles()){
            if(filter.apply(file) && (old == null || old.lastModified() <= file.lastModified()) )
                old = file;
        }
        return old;
    }
    ///Создаёт фильтр для сортировки уникальных значений
    ///@param <T> тип объекта, который будет подвергнут сортировке
    ///@param keyExtractor функция получения ключа из объекта. В идеале, получения хэша
    ///@return функцию фильтрации, которая определяет - был уже этот объект в наборе или нет
    public static <T> java.util.function.Predicate<T> distinct(java.util.function.Function<? super T, ?> keyExtractor) {
        var seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return t -> seen.add(keyExtractor.apply(t));
    }
    
    
	/**
	 * Пауза для текущего потока
	 * @param sec секунды
	 */
	public static void pause(long sec) {
		pause_ms(sec * 1_000);
	}
	/**
	 * Пауза для текущего потока
	 * @param msec милисекунды
	 */
	public static void pause_ms(long msec) {
		pause_ns(msec * 1_000_000);
	}
	/**
	 * Пауза для текущего потока
	 * @param nsec наносекунды
	 */
	public static void pause_ns(long nsec) {
		LockSupport.parkNanos(nsec);
	}
    /** @return true, если приложение запущено в ОС Windows*/
    public static boolean isWindows(){
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
    
    
    
    
    
    
    
    
    
    
    
    ///Фукнция, которая один раз предупреждает программиста, что где-то остался отладочный код в программе
    private static void debugCode(){
        if(isWriteToLogError){
            isWriteToLogError = false;
            Logger.getLogger(tools.class.getName()).log(Level.SEVERE, "Остался отладочный код!!!");
        }
    }
}
