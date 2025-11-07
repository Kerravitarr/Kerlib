/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.json;

import java.io.IOException;
import java.io.Writer;
import java.util.List;

///Интерфейс для любого параметра JSON. Для его записи в объект или для приведения типов
///
/// @author Kerravitarr (github.com/Kerravitarr)
class Serializer {
    /**
     * Записывает красиво (или не очень) форматированный объект в поток.
     * @param value_o объект, который надо записать
     * @param writer - цель, куда записывается объект
     * @param tabs - специальная переменная, позволяет сделать красивое форматирование.
     * 				Если она null, то форматирования не будет
     * @throws IOException - следует учитывать возможность выброса исключения при работе с файлом
     */
    public static <T> void write(T value_o, Writer writer, String tabs) throws IOException {
        if (value_o == null) {
            writer.write("null");
        } else if (value_o instanceof String s) {
            var sb = new StringBuffer();
            sb.append("\"");
            var len = s.length();
            for (var i = 0; i < len; i++) {
                var ch = s.charAt(i);
                switch (ch) {
                    case '"' -> sb.append("\\\"");
                    case '\\' -> sb.append("\\\\");
                    case '\b' -> sb.append("\\b");
                    case '\f' -> sb.append("\\f");
                    case '\n' -> sb.append("\\n");
                    case '\r' -> sb.append("\\r");
                    case '\t' -> sb.append("\\t");
                    case '/' -> sb.append("\\/");
                    default -> {
                        //Reference: http://www.unicode.org/versions/Unicode5.1.0/
                        if ((ch >= '\u0000' && ch <= '\u001F') || (ch >= '\u007F' && ch <= '\u009F') || (ch >= '\u2000' && ch <= '\u20FF')) {
                            var ss = Integer.toHexString(ch);
                            sb.append("\\u");
                            for (var k = 0; k < 4 - ss.length(); k++) {
                                sb.append('0');
                            }
                            sb.append(ss.toUpperCase());
                        } else {
                            sb.append(ch);
                        }
                    }
                }
            }
            sb.append("\"");
            writer.write(sb.toString());
        } else if (value_o instanceof Enum e) {
            writer.write("\"" + e.name() + "\"");
        } else if (value_o.getClass().isPrimitive() || value_o instanceof Number || value_o instanceof Boolean) {
            writer.write(String.valueOf(value_o));
        } else if (value_o instanceof JSON json) {
            if (tabs != null) {
                json.toBeautifulJSONString(writer, tabs);
            } else {
                json.toBeautifulJSONString(writer, null);
            }
        } else if (value_o instanceof List list) {
            if (list.isEmpty()) {
                writer.write("[]");
            } else {
                var fcl = list.get(0).getClass();
                if (!isBase(list.get(0)) || list.stream().anyMatch(v -> !v.getClass().equals(fcl))) {
                    //У нас сложные или разноплановые объекты
                    writer.write("[");
                    if (tabs != null) {
                        writer.write("\n");
                    }
                    var isFirst = true;
                    for (var value : list) {
                        if (isFirst) isFirst = false;
                        else if (tabs != null) writer.write(",\n");
                        else writer.write(",");
                        if (tabs != null) {
                            writer.write(tabs + "\t");
                            write(value, writer, tabs + "\t");
                        } else {
                            write(value, writer, null);
                        }
                    }
                    if (tabs != null) writer.write("\n" + tabs + "]");
                    else              writer.write("]");
                } else {
                    //У нас однородные примитивы
                    writer.write("[");
                    var isFirst = true;
                    for (var value : list) {
                        if (isFirst) isFirst = false;
                        else writer.write(",");
                        write(value, writer, null);
                    }
                    writer.write("]");
                }
            }
        } else {
            throw new IllegalArgumentException("Невозможно вывести объект типа" + value_o.getClass() + " -> " + value_o);
        }
    }

    /**Проверяет входящий тип на допустимость
     * @param <T>
     * @param value_o
     * @throws IllegalArgumentException если такой тип недопустим
     */
    public static <T> Object box(T value_o) {
        if (isBase(value_o) || value_o instanceof JSON) {
            return value_o;
        } else if (value_o instanceof List list) {
            return list.stream().map(value -> box(value)).toList();
        } else if (value_o.getClass().isArray()) {
            var length = java.lang.reflect.Array.getLength(value_o);
            return java.util.stream.IntStream.range(0, length).boxed().map(i -> box(java.lang.reflect.Array.get(value_o, i))).toList();
        } else {
            throw new IllegalArgumentException("Невозможно превести к JSON объект типа [" + value_o.getClass() + "] = " + value_o);
        }
    }

    /**Осуществляет преобразование из класса в класс
     * @param <T> итоговый класс
     * @param cls класс, который описывает то, к чему мы стремимся
     * @param o входной объект
     * @return объект, нужного типа
     * @throws ClassCastException когда не смогли преобразовать один тип к другому
     */
    public static <T> List<T> unboxl(Class<T> cls, Object o) throws ClassCastException {
        if (o == null) {
            return null;
        } else if (o instanceof List list) {
            return (List<T>) list.stream().map(v -> kerlib.tools.unbox(cls, v)).toList();
        } else {
            throw new IllegalArgumentException("Нельзя преобразовать значение к массиву");
        }
    }

    private static <T> boolean isBase(T value_o) {
        return value_o == null || value_o instanceof String || value_o instanceof Enum || value_o.getClass().isPrimitive() || value_o instanceof Number || value_o instanceof Boolean;
    }    
}
