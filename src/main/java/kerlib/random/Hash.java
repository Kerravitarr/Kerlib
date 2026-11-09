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

    @Override
    public String toString() {
        return Long.toString(hash);
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
    ///Алгоритм смешивания чисел на основе MurmurHash3
    /// @param seed Начальное число (сид) для инициализации миксера.
    /// @param numbers Массив или вариативный список чисел для миксования.
    /// @return Уникальное псевдослучайное число (хэш).
    public static int mix(int seed, int... numbers){
        if (numbers == null || numbers.length == 0) {
            return 0;
        }
        var hash = seed;
        for (var k : numbers) {
            k *= 0xcc9e2d51;
            k = Integer.rotateLeft(k, 15);
            k *= 0x1b873593;

            hash ^= k;
            hash = Integer.rotateLeft(hash, 13);
            hash = hash * 5 + 0xe6546b64;
        }
        int lengthInBytes = numbers.length * 4;
        hash ^= lengthInBytes;
        
        return scramble(hash);
    }
    ///Алгоритм смешивания чисел на основе MurmurHash3
    /// @param seed Начальное число (сид) для инициализации миксера.
    /// @param numbers Массив или вариативный список чисел для миксования.
    /// @return Уникальное псевдослучайное число (хэш).
    public static long mix(long seed, long... numbers){
        if (numbers == null || numbers.length == 0) {
            return 0;
        }
        var hash = seed;
        for (var k : numbers) {
            k *= 0xff51afd7ed558ccdL;
            k = Long.rotateLeft(k, 31);
            k *= 0xc4ceb9fe1a85ec53L;

            hash ^= k;
            hash = Long.rotateLeft(hash, 27);
            hash = hash * 5 + 0x52dce729;
        }
        long lengthInBytes = (long) numbers.length * 8;
        hash ^= lengthInBytes;
        
        return scramble(hash);
    }
    @Override
    public long nextLong() {
        return Hash.to(hash++);
    }
    
    
    
    
    
    /// Базовая функция перемешивания битов (MurmurHash3 константы)
    private static int scramble(int hash) {
        hash ^= hash >>> 16;
        hash *= 0x85ebca6b;
        hash ^= hash >>> 13;
        hash *= 0xc2b2ae35;
        hash ^= hash >>> 16;
        return hash;
    }

    /// Базовая функция перемешивания битов (MurmurHash3 константы)
    private static long scramble(long hash) {
        hash ^= hash >>> 33;
        hash *= 0xff51afd7ed558ccdL;
        hash ^= hash >>> 33;
        hash *= 0xc4ceb9fe1a85ec53L;
        hash ^= hash >>> 33;
        return hash;
    }

}
