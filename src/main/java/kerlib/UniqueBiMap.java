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

import java.util.HashMap;
import java.util.Map;

/**
 * Двунаправленная карта (BiMap) с уникальными ключами и значениями.
 *
 * <p>Хранит пары «ключ–значение» таким образом, что поиск работает
 * в обе стороны: по ключу → значение и по значению → ключ.
 * Уникальность гарантируется в обоих направлениях: ни один ключ,
 * ни одно значение не может встречаться в карте дважды.</p>
 *
 * <p>Метод {@link #put(Object, Object)} является потокобезопасным
 * (synchronized). Остальные методы не синхронизированы — при
 * конкурентном доступе оберните экземпляр во внешнюю синхронизацию
 * или используйте {@code synchronized}-блоки.</p>
 *
 * <p>Пример использования:</p>
 * <pre>{@code
 * UniqueBiMap<String, Integer> map = new UniqueBiMap<>();
 * map.put("one", 1);
 * map.put("two", 2);
 *
 * map.getByKey("one");   // → 1
 * map.getByValue(2);     // → "two"
 * map.removeByKey("one");
 * }</pre>
 *
 * @param <K> тип ключей
 * @param <V> тип значений
 *
 * @author Ilia Pushkin (github.com/Kerravitarr)
 */
public class UniqueBiMap<K, V> {

    /** Прямое отображение: ключ → значение. */
    private final Map<K, V> forward = new HashMap<>();

    /** Обратное отображение: значение → ключ. */
    private final Map<V, K> backward = new HashMap<>();

    /**
     * Добавляет пару «ключ–значение» в карту.
     *
     * <p>Оба направления обновляются атомарно в рамках одного
     * синхронизированного вызова, поэтому метод безопасен при
     * конкурентном использовании.</p>
     *
     * @param key   ключ; не должен уже присутствовать в карте
     * @param value значение; не должно уже присутствовать в карте
     * @throws IllegalArgumentException если {@code key} или {@code value}
     *                                  уже существуют в карте
     */
    public synchronized void put(K key, V value) {
        if (forward.containsKey(key) || backward.containsKey(value))
            throw new IllegalArgumentException("Ключ или Значение уже существуют!");
        forward.put(key, value);
        backward.put(value, key);
    }

    /**
     * Возвращает значение, связанное с указанным ключом.
     *
     * @param key ключ для поиска
     * @return соответствующее значение, или {@code null}, если ключ не найден
     */
    public V getByKey(K key) { return forward.get(key); }

    /**
     * Возвращает ключ, связанный с указанным значением.
     *
     * @param value значение для поиска
     * @return соответствующий ключ, или {@code null}, если значение не найдено
     */
    public K getByValue(V value) { return backward.get(value); }

    /**
     * Удаляет пару «ключ–значение» по ключу.
     *
     * <p>Если ключ не найден, метод завершается без каких-либо изменений.</p>
     *
     * @param key ключ удаляемой пары
     */
    public synchronized void removeByKey(K key) {
        V value = forward.remove(key);
        if (value != null) {
            backward.remove(value);
        }
    }
}
