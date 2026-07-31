///
/// The MIT License
///
/// Copyright 2026 Ilia Pushkin (github.com/Kerravitarr).
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in
/// all copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
/// THE SOFTWARE.
///

package kerlib;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

///Преобразователь систем счисления
///
///Конвертирует массив "цифр" из одной системы счисления в другую
///через промежуточное представление в виде большого числа.
///
///Внутреннее представление BigNumber: вектор uint32_t, младшие разряды первые (little-endian).
///Каждый элемент — "цифра" в системе счисления 2^32.
///
/// @author Ilia Pushkin (github.com/Kerravitarr)
public class RadixConverter {
    public static List<Integer> convert(byte[] digits,int factor,int newFactor) {
        return convert(java.util.stream.IntStream.range(0, digits.length).boxed().map(i -> Byte.toUnsignedLong(digits[i])).toList(), factor , newFactor);
    }
    /**
    * Конвертирует массив чисел из одной системы счисления в другую.
    *
    * @param digits исходный массив цифр (каждый элемент < factor)
    * @param factor исходная система счисления (основание)
    * @param newFactor целевая система счисления (основание)
    * @return список цифр в новой системе счисления
    */
    public static List<Integer> convert(List<? extends Number> digits,int factor,int newFactor) {
        // Шаг 1: Собираем все цифры в одно большое число (BigInteger)
        // number = d[0]*factor^(n-1) + d[1]*factor^(n-2) + ... + d[n-1]
        var value = BigInteger.ZERO;
        var bigFactor = BigInteger.valueOf(factor);
        for (var digit : digits) {
            value = value.multiply(bigFactor).add(BigInteger.valueOf(digit.longValue()));
        }
        // Шаг 2: Раскладываем число в новую систему счисления
        // Делим с остатком на newFactor, остатки — цифры (с конца)
        if (value.equals(BigInteger.ZERO)) {
            return Collections.singletonList(0);
        }
        var result = new ArrayList<Integer>();
        var bigNewFactor = BigInteger.valueOf(newFactor);
        while (value.compareTo(BigInteger.ZERO) > 0) {
            var divAndRem = value.divideAndRemainder(bigNewFactor);
            result.add(0, divAndRem[1].intValue()); // остаток — очередная цифра
            value = divAndRem[0]; // частное — для следующей итерации
        }
        return result;
    }
}
