/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SplittableRandom;

/**
 * Генератор цепочек Маркова
 *
 * @author Kerravitarr
 * @param <T> Что мы генерируем
 * @param <E> Из каких элементов это нечто состоит
 */
public class Markov<T, E> {
	/**Представляет цепочку элементов в цепи Маркова.*/
	private static class Chain {
		private final List<Object> chain;

		/**
		 * Создает цепочку из списка элементов.
		 * @param chain список элементов
		 */
		public Chain(List<Object> chain) {
			this.chain = new ArrayList<>(chain);
		}
		
		/**
		 * Создает цепочку из одного элемента.
		 * @param chain элемент
		 */
		public Chain(Object chain) {
			this.chain = new ArrayList<>(){{add(chain);}};
		}
		
		/**
		 * Возвращает первый элемент цепочки.
		 * @return первый элемент
		 */
		public Object start(){
			return chain.get(0);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof Chain ch)
				return this == ch || chain.equals(ch.chain);
			else
				return super.equals(obj);
		}

		@Override
		public int hashCode() {
			int hash = 3;
			hash = 61 * hash + Objects.hashCode(this.chain);
			return hash;
		}

		@Override public String toString() {return chain.toString();}		
	}
	/** Узел цепи Маркова */
	private class Node {
		/** Таблица перехода от одного узла к следующему. К какому узлу и с какой вероятностью */
		private Map<Node, Integer> _next = new HashMap<>();
		/** Сколько всего переходов может быть, тут сумма по всем int сверху */
		private int allJmp = 0;
		/** Что за узел */
		private final Chain val;

		private Node(Chain v) {
			val = v;
		}

		/**
		 * Добавляет следующий узел к дереву переходов.
		 * @param next следующий узел
		 */
		public void addNext(Node next) {
			if (!_next.containsKey(next)) {
				_next.put(next, 0);
			}
			_next.put(next, _next.get(next) + 1);
			allJmp++;
		}

		/**
		 * Возвращает следующий элемент на основе вероятностного распределения.
		 * @return следующий элемент цепи
		 * @throws RuntimeException если не удалось получить следующий элемент
		 */
		public Object getNext() {
			var retvalue = rnd.nextInt(allJmp);
			for (var i : _next.entrySet()) {
				if (i.getValue() > retvalue) {
					return i.getKey().val.start();
				} else {
					retvalue -= i.getValue();
				}
			}
			throw new RuntimeException("Не смогли получить следующий символ");
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(getVal());
			sb.append(" -> ");
			sb.append("[");
			_next.entrySet().stream().sorted(Map.Entry.<Node, Integer>comparingByValue().reversed()).forEach(v -> {
				sb.append(v.getKey().getVal());
				sb.append("(");
				sb.append(v.getValue() * 100 / allJmp);
				sb.append("%) ");
			});
			sb.append("]");
			return sb.toString();
		}

		/**
		 * Рекурсивно выводит дерево переходов в текстовом виде.
		 * @param buffer буфер для записи результата
		 * @param probability вероятность перехода к этому узлу
		 * @param prefix префикс для текущего узла
		 * @param childrenPrefix префикс для дочерних узлов
		 * @param deep глубина вывода
		 */
		private void print(StringBuilder buffer, double probability, String prefix, String childrenPrefix, int deep) {
			buffer.append(prefix);
			buffer.append(getVal());
			buffer.append(String.format("(%.1f%%)\n", probability));
			var list = _next.entrySet().stream().sorted(Map.Entry.<Node, Integer>comparingByValue().reversed()).toList();
			for (var it = list.iterator(); it.hasNext() && deep > 0;) {
				var next = it.next();
				if (it.hasNext()) {
					next.getKey().print(buffer, next.getValue() * 100.0 / allJmp, childrenPrefix + "├── ", childrenPrefix + "│   ", deep - 1);
				} else {
					next.getKey().print(buffer, next.getValue() * 100.0 / allJmp, childrenPrefix + "└── ", childrenPrefix + "    ", deep - 1);
				}
			}
		}

		/**
		 * Возвращает строковое представление значения узла.
		 * @return строковое представление
		 */
		public String getVal() {
			var s = val.start();
			if(s == START_ELEMENT) return "S";
			else if(s == END_ELEMENT) return "E";
			else return val.toString();
		}
	}

	/** Анализирует входящий массив объектов, для создания объекта
	 * @param partL Сколько должно быть элементов в цепочке, максимум (максимальная длина выходной конструкции)
	 * @param words весь корпус слов, для анализа
	 * @param split функция разбиения целого элемента на кусочки
	 * @param join функция объединения кусочков элемента до целого
	 */
	public Markov(int partL, T[] words, java.util.function.Function<T, List<E>> split, java.util.function.Function<List<E>, T> join) {
		MAX_LENGHT = partL;
		this.split = split;
		this.join = join;
		var m = 0;
		var chain = new ArrayList<Object>();
		for (var word : words) {
            try{
                var elements = split.apply(word);
                m += elements.size();

                chain.clear();
                chain.add(START_ELEMENT);
                for (var c : elements) {
                    for (var i = chain.size()- 1; i >= Math.max(chain.size() - MAX_LENGHT, 0); i--) {
                        var pref = chain.subList(i, chain.size());
                        addElement(pref, c);
                    }
                    chain.add(c);
                }
                for (var i = chain.size() - 1; i >= Math.max(chain.size() - MAX_LENGHT, 0); i--) {
                    var pref = chain.subList(i, chain.size());
                    addElement(pref, END_ELEMENT);
                }
            } catch(Exception ex){
                
            }
		}
		if (words.length > 0) {
			MEDIAN = m / words.length;
		} else {
			MEDIAN = 0;
		}
	}
	/**Создаёт генератор цепей Маркова для строк
	 * @param partL максимальная длина выходной строки (сколько максимум букв)
	 * @param words корпус слов для обучения
	 * @return генератор цепей Маркова для строк
	 */
	public static Markov<String, String> strings(int partL, String[] words){
		return new Markov<>(partL, words, v -> List.of(v.split("")), v -> String.join("", v));
	}

	/**
	 * Создает одно слово на основе словаря с параметрами по умолчанию.
	 * @return сгенерированное слово
	 */
	public T generate() {
		return generate(null, 4, 1000);
	}

	/**
	 * Создает одно слово на основе словаря с заданными параметрами.
	 * @param start начало слова, которое нужно продолжить (может быть null)
	 * @param minL минимальная длина слова
	 * @param maxL максимальная длина слова
	 * @return сгенерированное слово
	 */
	public T generate(T start, int minL, int maxL) {
		var chain = new ArrayList<Object>();
		for (int i = 0; i < 10 && chain.size() - 1 < minL; i++) {
			chain = new ArrayList<>();
			chain.add(START_ELEMENT);
			if (start != null) {
				for(var e : split.apply(start))
					chain.add(e);
			}
			while (chain.get(chain.size() - 1) != END_ELEMENT) {
				int minLSlog = 2;
				int j;
				for (j = 0; j < 10; j++) {
					var l = Math.min(Math.max(minLSlog, rnd.nextInt(MAX_LENGHT + 1)), chain.size());
					var pref = chain.subList(chain.size() - l, chain.size());
					var p = new Chain(pref);
					if (table.containsKey(p)) {
						var next = table.get(p);
						if (next.allJmp > 0) {
							chain.add(next.getNext());
							break;
						}
					}
					if (minLSlog > 0) {
						minLSlog--;
					}
				}
				if ((j == 10 || chain.size() - 1 >= maxL) && chain.get(chain.size() - 1) != END_ELEMENT) {
					chain.add(END_ELEMENT);
				}
			}
		}
		return this.join.apply((List<E>)chain.subList(1, chain.size() - 1).stream().toList());
	}

	/**
	 * Добавляет новый элемент в цепь Маркова.
	 * @param pref предыдущие элементы (префикс)
	 * @param c новый элемент
	 */
	private void addElement(List<Object> pref, Object c) {
		var p = new Chain(pref);
		var n = new Chain(c);
		if (!table.containsKey(p))
			table.put(p, new Node(p));
		if (!table.containsKey(n)) 
			table.put(n, new Node(n));
		table.get(p).addNext(table.get(n));
	}

	@Override
	public String toString() {
		return print(1);
	}

	/**
	 * Выводит дерево переходов цепи Маркова в текстовом виде.
	 * @param deep глубина вывода дерева
	 * @return строковое представление дерева
	 */
	public String print(int deep) {
		var sb = new StringBuilder();
		var start = new Chain(START_ELEMENT);
		if (table.containsKey(start)) {
			table.get(start).print(sb, 100, "", "", deep);
		} else {
			sb.append("S->E");
		}
		return sb.toString();
	}

	/** Таблица всех переходов между состояниями цепи Маркова */
	private Map<Chain, Node> table = new HashMap<>();
	/** Средняя длина элементов в обучающем наборе */
	public final int MEDIAN;
	/** Максимальная длина цепочки для анализа переходов */
	private final int MAX_LENGHT;
	/** Функция разбиения исходного объекта на элементы */
	private final java.util.function.Function<T, List<E>> split;
	/** Функция объединения элементов в исходный объект */
	private final java.util.function.Function<List<E>, T> join;
	/** Маркер начала последовательности */
	private static final Object START_ELEMENT = new Object(){@Override public String toString(){return "START_ELEMENT";}};
	/** Маркер конца последовательности */
	private static final Object END_ELEMENT = new Object(){@Override public String toString(){return "END_ELEMENT";}};
	/** Генератор случайных чисел для выбора переходов */
	private static final SplittableRandom rnd = new SplittableRandom();
}
