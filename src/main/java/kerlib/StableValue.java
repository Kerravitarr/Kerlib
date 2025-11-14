/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib;

/**
 *Пока он находится в превью, сделал для себя временную копию
 * @author Kerravitarr (github.com/Kerravitarr)
 */
public class StableValue{
    
    public static <T> java.util.function.Supplier<T> supplier(java.util.function.Supplier<T> getter){
        return new java.util.function.Supplier() {
            ///Значение уже сохранялось?
            private boolean isSet = false;
            ///Возвращаемое значение
            private T ret = null;
            @Override
            public Object get() {
                if(!isSet){
                    ret = getter.get();
                    isSet = true;
                }
                return ret;
            }
        };
    }
}
