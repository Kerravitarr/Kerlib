/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib;

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
}
