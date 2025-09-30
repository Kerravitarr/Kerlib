/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.random;

import java.util.random.RandomGenerator;

/**Генератор случайных чисел на основе некоторого другого числа
 * ВНИМАНИЕ!!!!
 * Весь этот пакет создан для создания самых простых случайностей, обратимых,
 * Тут нет ни криптографической стойкости, ни истиной случайности!!!
 * @author Kerravitarr (github.com/Kerravitarr)
 */
public class Hash implements RandomGenerator{
    ///Хэш, который по факту только суммируется
    private long hash;

    public Hash(long hash) {
        this.hash = hash;
    }
    
    ///Возвращает случайное значение перечисления
    public <T extends Enum> T next(Class<T> enm){
        return next(enm.getEnumConstants());
    }
    ///Возвращает случайное значение из массива
    public <T> T next(T[] arr){
        return arr[nextInt(arr.length)];
    }
    
    
    /**
	 * Вычисляет обратимый хэш для числа.
	 * Взято с ответов - https://stackoverflow.com/a/12996028/22441496
	 * @param x число
	 * @return обратимый хэш этого числа
	 */
	public static int to(int x){
		x = ((x >>> 16) ^ x) * 0x45d9f3b;
		x = ((x >>> 16) ^ x) * 0x45d9f3b;
		x = (x >>> 16) ^ x;
		return x;
	}
	/**
	 * Вычисляет обратимый хэш для числа.
	 * Взято с ответов - https://stackoverflow.com/a/12996028/22441496
	 * @param x число
	 * @return обратимый хэш этого числа
	 */
	public static long to(long x){
		x = (x ^ (x >>> 30)) * (0xbf58476d1ce4e5b9L);
		x = (x ^ (x >>> 27)) * (0x94d049bb133111ebL);
		x = x ^ (x >>> 31);
		return x;
	}
	/**
	 * Вычисляет число из обратного хэша
	 * Взято с ответов - https://stackoverflow.com/a/12996028/22441496
	 * @param x хэш
	 * @return число
	 */
	public static int from(int x){
		x = ((x >>> 16) ^ x) * 0x119de1f3;
		x = ((x >>> 16) ^ x) * 0x119de1f3;
		x = (x >>> 16) ^ x;
		return x;
	}
	/**
	 * Вычисляет число из обратного хэша
	 * Взято с ответов - https://stackoverflow.com/a/12996028/22441496
	 * @param x хэш
	 * @return число
	 */
	public static long from(long x){
		x = (x ^ (x >>> 31) ^ (x >>> 62)) * (0x319642b2d24d8ec3L);
		x = (x ^ (x >>> 27) ^ (x >>> 54)) * (0x96de1b173f119089L);
		x = x ^ (x >>> 30) ^ (x >>> 60);
		return x;
	}

    @Override
    public long nextLong() {
        return Hash.to(hash++);
    }
}
