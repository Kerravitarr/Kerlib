package kerlib.json;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;


///Класс, который отвечает за объекты типа JSON
/// @author Kerravitarr (github.com/Kerravitarr)
public final class JSON{
	/** Создаёт пустой объект JSON */
	public JSON(){
		parametrs = new LinkedHashMap<>();
	}
	/**Парсинг JSON строки
	 * @param parseStr строка, которую разбираем
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 */
	public JSON(String parseStr) throws ParseException {
        this();
		try {
			parse(new StringReader(parseStr));
		} catch (IOException ex) { //Быть не может! Стркоу нельзя так прочитать!
			throw new RuntimeException(ex);
		}
	}
	/**Парсинг JSON потока
	 * @param in поток чтения
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws IOException ошибка разбора, ошибка устройства чтения
	 */
	public JSON(Reader in) throws ParseException, IOException {
        this();
		parse(in);
	}
	/**Парсинг JSON строки в массив
	 * @param <T>
	 * @param cls класс, который мы хотим получить
	 * @param parseStr строка, которую разбираем
	 * @return массив разобранных объектов. 
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public static <T> List<T> parse(Class<T> cls,String parseStr) throws ParseException {
		try {
			return parse(cls, new StringReader(parseStr));
		} catch (IOException ex) { //Быть не может! Стркоу нельзя так прочитать!
			throw new RuntimeException(ex);
		}
	}
	/**Парсинг JSON строки в массив
	 * @param <T>
	 * @param cls класс, который мы хотим получить
	 * @param in поток чтения
	 * @return массив разобранных объектов. 
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws IOException ошибка разбора, ошибка устройства чтения
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public static <T> List<T> parse(Class<T> cls,Reader in) throws ParseException, IOException {
		var reader = new TokenReader(in);
		if(!reader.hasNext()) { // Пустой файл
			return new ArrayList<>();
		} else {
			var token = reader.next();
			if(token.type == JSON_TOKEN.BEGIN_ARRAY)
				return Serializer.unboxl(cls, parseA(reader));
			else
				throw new ParseException(reader.pos, ERROR.UNEXPECTED_TOKEN, token.value);
		}
	}
	
	/** Добавить новую пару ключ-значение в объект
	 * @param <T>
	 * @param key ключ
	 * @param value значение
	 * @return текущий объект для возможности создания цепочек
	 */
	public <T> JSON add(String key, T value) {
		parametrs.put(key, Serializer.box(value));
		return this;
	}
    ///Удаляет ключ из объекта
    ///@param key ключ
    ///@return предыдущее значение, связанное с ключом, или null, если сопоставление для ключа не было. (Возвращаемое значение null также может указывать на то, что сопоставление ранее связывало null с ключом.)
    public Object remove(String key){
        return parametrs.remove(key);
    }
	/**Получает значение по ключу
	 * @param <T>
	 * @param cls ожидаемый класс
	 * @param key ключ
	 * @return значение, или null, если значение не найдено
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public <T> T get(Class<T> cls, String key) throws IllegalArgumentException {
		return get(cls, key, (java.util.function.Supplier<T>) null);
	}
	/**Получает значение по ключу
	 * @param <T>
	 * @param cls ожидаемый класс
	 * @param key ключ
	 * @param def значение по умолчанию. Если функция не задана, то по умолчанию будет значение null
	 * @return значение, или null, если значение не найдено
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public <T> T get(Class<T> cls, String key, java.util.function.Supplier<T> def) throws IllegalArgumentException {
		return kerlib.tools.unbox(cls,get(key, def));
	}
	/**Получает значение по ключу
	 * @param <T>
	 * @param cls ожидаемый класс
	 * @param key ключ
	 * @param def значение по умолчанию
	 * @return значение, или null, если значение не найдено
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public <T> T getDef(Class<T> cls, String key, T def) throws IllegalArgumentException {
		return get(cls, key, (java.util.function.Supplier<T>) () -> def);
	}
	/**Получает значение массива по ключу
	 * @param <T>
	 * @param cls - ожидаемый класс элементов
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 * @throws IllegalArgumentException возникает, если элемент представляет единственное значение и вернуть как массив его нельзя
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public <T> List<T> getA(Class<T> cls, String key) {
		return getA(cls,key, (java.util.function.Supplier<List<T>>) null);
	}
	/**Получает значение массива по ключу
	 * @param <T>
	 * @param cls - ожидаемый класс элементов
	 * @param key - ключ
	 * @param def значение по умолчанию. Если функция не задана, то по умолчанию будет значение null
	 * @return - значение, или null, если значение не найдено
	 * @throws IllegalArgumentException возникает, если элемент представляет единственное значение и вернуть как массив его нельзя
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public <T> List<T> getA(Class<T> cls, String key,java.util.function.Supplier<List<T>> def) {
		return Serializer.unboxl(cls,get(key,def));
	}
	/**Получает значение массива по ключу
	 * @param <T>
	 * @param cls - ожидаемый класс элементов
	 * @param key - ключ
	 * @param def значение по умолчанию
	 * @return - значение, или null, если значение не найдено
	 * @throws IllegalArgumentException возникает, если элемент представляет единственное значение и вернуть как массив его нельзя
	 * @throws ClassCastException возникает, когда возвращаемое значение довольно сильно отличается от желаемого
	 */
	public <T> List<T> getADef(Class<T> cls, String key, List<T> def) {
		return getA(cls,key, () -> def);
	}
	/**Получает массив JSON по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 * @throws IllegalArgumentException возникает, если массив не состоит только из JSON
	 */
	public List<JSON> getAJ(String key) {
		return getA(JSON.class, key);
	}
	/**Получает значение по ключу
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 * @throws IllegalArgumentException возникает, если элемент не состоит из JSON
	 */
	public JSON getJ(String key) {
		return get(JSON.class, key);
	}
	/**Приводит JSON объект к строке
	 * @return Одна простая и длинная строка без форматирвоания
	 */
	public String toJSONString() {
		try {
			var sw = new StringWriter();
			toJSONString(sw);		
			return sw.toString();
		} catch (IOException e) {throw new RuntimeException(e);} // Быть такого не может! Не должен SW давать ошибки IO
	}
	/** Приводит JSON объект к строке - простой и длинной строке без форматирвоания.И дописывает её в конец
	 * @param writer
	 * @throws IOException 
	 */
	public void toJSONString(Writer writer) throws IOException {
		toBeautifulJSONString(writer,null);
		writer.flush();
	}
	/** Приводит JSON объект к строке
	 * @return строка, форматированная согласно правилам составления JSON объектов, с табами и подобным
	 */
	public String toBeautifulJSONString() {
		try {
			var sw = new StringWriter();
			toBeautifulJSONString(sw);
			return sw.toString();
		} catch (IOException e) {throw new RuntimeException(e);} // Быть такого не может! Не должен SW давать ошибки IO
	}
	/** Приводит JSON объект к строке, форматированной согласно правилам составления JSON объектов, с табами и подобным.Дописывает в конец документа
	 * @param writer
	 * @throws IOException 
	 */
	public void toBeautifulJSONString(Writer writer) throws IOException {
		toBeautifulJSONString(writer,"");
		writer.flush();
	}
	/**Проверяет наличие ключа в объекте
	 * @param key ключ
	 * @return true, если ключ тут есть
	 */
	public boolean containsKey(String key) { return parametrs.containsKey(key);}

	/**Очищает все элементы объекта*/
	public void clear() { parametrs.clear(); }
	/**Возвращает список всех ключей объекта
	 * @return список со всеми ключами
	 */
	public Set<String> getKeys(){ return parametrs.keySet(); }
	@Override
	public String toString() { return toJSONString(); }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        else if(!(obj instanceof JSON)) return false;
        else {
            var eq = (JSON) obj;
            //Фильтруем не уникальные поля. Их быть не должно!
            return parametrs.entrySet().stream().filter(entry -> {
                var eq_get = eq.parametrs.get(entry.getKey());
                var this_get = entry.getValue();
                if(java.util.Objects.deepEquals(eq_get, this_get)) return false;
                else if(eq_get == null || this_get == null) return true;
                else {
                    try{
                        return !eq_get.equals(kerlib.tools.unbox(eq_get.getClass(),this_get));
                    } catch (Exception _){
                        return true;
                    }
                }
            }).findAny().isEmpty();
        }
    }
    
		
	
	/**Парсинг JSON строки
	 * @param parseStr строка, которую разбираем
	 * @return массив разобранных объектов. 
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 */
	@Deprecated
	public static List<Object> JSONA(String parseStr) throws ParseException {
		try {
			return JSONA(new StringReader(parseStr));
		} catch (IOException ex) { //Быть не может! Стркоу нельзя так прочитать!
			throw new RuntimeException(ex);
		}
	}
	/**Парсинг JSON строки и заполнение соответствующих объектов
	 * @param in поток чтения
	 * @return массив разобранных объектов. 
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws IOException ошибка разбора, ошибка устройства чтения
	 */
	@Deprecated
	public static List<Object> JSONA(Reader in) throws ParseException, IOException {
		var reader = new TokenReader(in);
		if(!reader.hasNext()) { // Пустой файл
			return new ArrayList<>();
		}else {
			var token = reader.next();
			if(token.type == JSON_TOKEN.BEGIN_ARRAY)
				return parseA(reader);
			else
				throw new ParseException(reader.pos, ERROR.UNEXPECTED_TOKEN, token.value);
		}
	}
	
	/**
	 * Возвращает массив состоящий из лонгов
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public List<Long> getAL(String key) {
		return getA(Long.class, key);
	}
	/**
	 * Получает значение по ключу. Заглушка, потому что во
	 * 	время исполнения не определить запрашиваемый тип
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public int getI(String key) {
		return get(int.class, key);
	}
	/**
	 * Получает значение по ключу. Заглушка, потому что во
	 * 	время исполнения не определить запрашиваемый тип
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public long getL(String key) {
		return get(long.class, key);
	}
	/**
	 * Получает значение по ключу. Заглушка, потому что во
	 * 	время исполнения не определить запрашиваемый тип
	 * @param key - ключ
	 * @return - значение, или null, если значение не найдено
	 */
	public double getD(String key) {
		return get(double.class, key);
	}
	/**
	 * Получает значение по ключу
	 * @param <T>
	 * @param key - ключ
	 * @deprecated теперь надо пользоваться методами с указанием класса объекта
	 * @return - значение, или null, если значение не найдено или значение не является единственным, а, например, это массив
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public <T> T get(String key) {
		return (T) parametrs.get(key);
	}
	/**
	 * Получает любые векторные значения по ключу
	 * @param <T>
	 * @param key - ключ
	 * @deprecated теперь надо пользоваться фукнцией с указанием класса объектов
	 * @return - значение, или null, если значение не найдено
	 */
	@SuppressWarnings("unchecked")
	@Deprecated
	public <T> List<T> getA(String key) {
		return (List<T>) parametrs.get(key);
	}
	/**Возвращает объект с учётом значения по умолчанию
	 * @param key ключ объекта
	 * @param def значение по умолчанию
	 * @return значение из объекта
	 */
	private Object get(String key, java.util.function.Supplier def){
		var o = parametrs.get(key);
		if(o == null) return def == null ? null : def.get();
		else return o;
	}
	
	/**Внутренний метод для печати объекта. Объект состоит из открывающей табы ну и дальше по тексту*/
	void toBeautifulJSONString(Writer writer,String tabs) throws IOException {
		writer.write("{");
		if(tabs != null)
			writer.write("\n");
		var isFirst = true;
		for (var param : parametrs.entrySet()) {
			if(isFirst) isFirst = false;
			else if(tabs != null) writer.write(",\n");
			else writer.write(",");
			if(tabs != null) {
				writer.write(tabs + "\t");
				writer.write("\"" + param.getKey() + "\": ");
				Serializer.write(param.getValue(), writer, tabs + "\t");
			} else {
				writer.write("\"" + param.getKey() + "\":");
				Serializer.write(param.getValue(), writer, null);
			}
		}
		if(tabs != null)
			writer.write("\n" + tabs);
		writer.write("}");
	}
	
	/**
	 * Разбирает поток в формат JSON
	 * @param in поток чтения
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws IOException ошибка разбора, ошибка устройства чтения
	 */
	private void parse(Reader in) throws IOException, ParseException{
		var reader = new TokenReader(in);
		if(!reader.hasNext()) { // Пустой файл
			parametrs.clear();
		} else {
			var token = reader.next();
			if(token.type == JSON_TOKEN.BEGIN_OBJECT)
				parametrs = parseO(reader).parametrs;
			else
				throw new ParseException(reader.pos, ERROR.UNEXPECTED_TOKEN, token.value);
		}
	}
	/**
	 * Парсит объект JSON, первый символ { уже получили
	 * @param reader
	 * @return
	 * @throws JSON.ParseException ошибка разбора, синтаксическая
	 * @throws IOException ошибка разбора, ошибка устройства чтения
	 */
	private static JSON parseO(TokenReader reader) throws ParseException, IOException {
		var json = new JSON();
		var expectToken = JSON_TOKEN.STRING.value | JSON_TOKEN.END_OBJECT.value; // Ключ или конец объекта
		String key = null;
		var lastToken = JSON_TOKEN.BEGIN_OBJECT;
		while (reader.hasNext()) {
			var token = reader.next();
			if ((expectToken & token.type.value) == 0)
				throw new ParseException(reader.pos, ERROR.UNEXPECTED_TOKEN, token.type);
			switch (token.type) {
				case BEGIN_ARRAY -> {
					json.add(key, parseA(reader));
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_OBJECT.value; // Или следующий объект или мы всё
				}
				case BEGIN_OBJECT -> {
					json.add(key, parseO(reader));
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_OBJECT.value; // Или следующий объект или мы всё
				}
				case END_ARRAY -> {
				}
				case END_DOCUMENT -> // Этого мы ни когда не ждём!
					throw new ParseException(reader.pos, ERROR.UNEXPECTED_EXCEPTION, "Неожиданный конец документа");
				case END_OBJECT -> {
					return json; //Мы всё!
				}
				case NULL -> {
					json.add(key, (Object) null);
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_OBJECT.value; // Или следующий объект или мы всё
				}
				case BOOLEAN, NUMBER -> {
					json.add(key, token.value);
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_OBJECT.value; // Или следующий объект или мы всё
				}
				case SEP_COLON -> expectToken = JSON_TOKEN.NULL.value | JSON_TOKEN.NUMBER.value | JSON_TOKEN.BOOLEAN.value
						| JSON_TOKEN.STRING.value | JSON_TOKEN.BEGIN_OBJECT.value | JSON_TOKEN.BEGIN_ARRAY.value; // А дальше значение ждём!
				case SEP_COMMA -> expectToken = JSON_TOKEN.STRING.value; // Теперь снова ключ
				case STRING -> {
					if(lastToken == JSON_TOKEN.SEP_COLON) { // Если у нас было :, то мы просто значение 
						json.add(key, token.value);
						expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_OBJECT.value; // Или следующий объект или мы всё
					} else { //А раз нет - то мы ключ
						key = (String) token.value;
						expectToken = JSON_TOKEN.SEP_COLON.value; // А дальше значение ждём!
					}
				}
			}
			lastToken = token.type;
		}
		throw new ParseException(reader.pos, ERROR.UNEXPECTED_EXCEPTION, "Неожиданный конец документа");
	}
	/**
	 * Парсит массив JSON, первый символ [ уже получили
	 * @param reader
	 * @return
	 * @throws JSON.ParseException
	 * @throws IOException
	 */
	private static List<Object> parseA(TokenReader reader) throws ParseException, IOException {
		var array = new ArrayList();
		var expectToken = JSON_TOKEN.BEGIN_ARRAY.value | JSON_TOKEN.END_ARRAY.value | JSON_TOKEN.BEGIN_OBJECT.value
				 | JSON_TOKEN.NUMBER.value | JSON_TOKEN.BOOLEAN.value | JSON_TOKEN.STRING.value | JSON_TOKEN.NULL.value; // Массив чего у нас там?
		while (reader.hasNext()) {
			var token = reader.next();
			if ((expectToken & token.type.value) == 0)
				throw new ParseException(reader.pos, ERROR.UNEXPECTED_TOKEN, token.value);
			switch (token.type) {
				case BEGIN_ARRAY -> {
					array.add(parseA(reader));
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_ARRAY.value; // Или следующий объект или мы всё
				}
				case BEGIN_OBJECT -> {
					array.add(parseO(reader));
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_ARRAY.value; // Или следующий объект или мы всё
				}
				case END_ARRAY -> {
					return array;
				}
				case END_OBJECT, SEP_COLON, END_DOCUMENT -> // Этого мы ни когда не ждём!
					throw new ParseException(reader.pos, ERROR.UNKNOW, "Ошибка библиотеки");
				case NULL -> {
					array.add(null);
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_ARRAY.value; // Или следующий объект или мы всё
				}
				case BOOLEAN, NUMBER, STRING -> {
					array.add(token.value);
					expectToken = JSON_TOKEN.SEP_COMMA.value | JSON_TOKEN.END_ARRAY.value; // Или следующий объект или мы всё
				}
				case SEP_COMMA -> expectToken = JSON_TOKEN.NULL.value | JSON_TOKEN.NUMBER.value | JSON_TOKEN.BOOLEAN.value
						| JSON_TOKEN.STRING.value | JSON_TOKEN.BEGIN_OBJECT.value | JSON_TOKEN.BEGIN_ARRAY.value; // А дальше значение ждём!
			}
		}
		throw new ParseException(reader.pos, ERROR.UNEXPECTED_EXCEPTION, "Неожиданный конец документа");
	}
	
	
	
	/**Это список всех параметров объекта. Используется лист пар потому что было важное условие - сохранить порядок данных*/
	private LinkedHashMap<String,Object> parametrs;
}
