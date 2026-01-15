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

package kerlib.draw;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextPane;
import javax.swing.JToolTip;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

/**
 * Расширенная текстовая панель с поддержкой всплывающих подсказок и стилизации текста.
 * Позволяет добавлять подсказки к определенным участкам текста и применять стили форматирования.
 * 
 * @author Ilia Pushkin (github.com/Kerravitarr)
 */
public class ITextPane extends JTextPane {
	/**
	 * Класс для построения цепочки стилей форматирования текста.
	 * Поддерживает цвет, жирность, подчеркивание и другие атрибуты.
	 */
	public static class Style{
        /** Типы полей стилей */
        private enum FieldType{
            BOOL,
            COLOR,
            INT,
            ;            
            /**
             * Определяет тип поля по значению.
             * @param value значение для определения типа
             * @return тип поля
             * @throws IllegalArgumentException если тип неизвестен
             */
            private static FieldType get(Object value){
                if(value != null){
                    var cls = value.getClass();
                    if(cls.equals(Boolean.class) || cls.equals(boolean.class)) return BOOL;
                    else if(cls.equals(Integer.class) || cls.equals(int.class)) return INT;
                    else if(cls.equals(Color.class)) return COLOR;
                }
                throw new IllegalArgumentException("Неизвестный тип " + (value == null ? null : value.getClass()));
            }
        }

        /** Создает пустой стиль */
        public Style(){this(null,null);}
        
        /**
         * Создает стиль с одним атрибутом.
         * @param styleConstantField константа стиля из StyleConstants
         * @param value значение атрибута
         */
        public Style(Object styleConstantField, Object value) {this(styleConstantField, value,null);}
        
        /**
         * Добавляет следующий атрибут стиля в цепочку.
         * @param styleConstantField константа стиля из StyleConstants
         * @param value значение атрибута
         * @return новый стиль с добавленным атрибутом
         */
        public Style next(Object styleConstantField, Object value){
            return new Style(styleConstantField, value, this);
        }
        
        /**
         * Устанавливает цвет текста.
         * @param c цвет
         * @return новый стиль с цветом
         */
        public Style foreground(Color c){return next(StyleConstants.Foreground, c);}
        
        /**
         * Устанавливает жирность текста.
         * @param isBold true для жирного текста
         * @return новый стиль с жирностью
         */
        public Style bold(boolean isBold){return next(StyleConstants.Bold, isBold);}
        
        /**
         * Делает текст жирным.
         * @return новый стиль с жирным текстом
         */
        public Style bold(){return bold(true);}
        
        /**
         * Устанавливает подчеркивание текста.
         * @param isUnderline true для подчеркнутого текста
         * @return новый стиль с подчеркиванием
         */
        public Style underline(boolean isUnderline){return next(StyleConstants.Underline, isUnderline);}
        
        /**
         * Делает текст подчеркнутым.
         * @return новый стиль с подчеркиванием
         */
        public Style underline(){return underline(true);}
        
        protected Style(Object styleConstantField, Object value, Style next) {
            this.styleConstantField = styleConstantField;
            this.value = value;
            this.next = next;
        }
        
        /**
         * Применяет стиль к набору атрибутов.
         * @param a набор атрибутов для модификации
         * @return функция для отмены изменений
         */
        public java.util.function.Consumer<SimpleAttributeSet> set(SimpleAttributeSet a){
            if(value == null){
                if(next != null) return next.set(a);
                else return _ -> {};
            }
            var t = FieldType.get(value);
            var old = oldDate(a,t, styleConstantField);
            a.addAttribute(styleConstantField, value);
            if(next != null){
                var nextUnset = next.set(a);
                return unset -> {
                    a.addAttribute(styleConstantField, old);
                    nextUnset.accept(a);
                };
            } else {
                return unset -> a.addAttribute(styleConstantField, old);
            }
        }
        
        
        /** Возвращает старое значение атрибута перед изменением */
        private static Object oldDate(SimpleAttributeSet a,FieldType type,  Object name){
            switch (type) {
                case BOOL -> {
                    var bold = (Boolean) a.getAttribute(name);
                    if (bold != null) return bold.booleanValue();
                    else return false;
                }
                case COLOR -> {
                    var fg = (Color) a.getAttribute(name);
                    if (fg == null) fg = Color.black;
                    return fg;
                }
                default ->
                    throw new IllegalArgumentException(String.valueOf(type));
            }
        }
        /**константа стиля из StyleConstants. */
        private final Object styleConstantField;
        /**значение атрибута. */
        private final Object value;
        /**следующий стиль. */
        private final Style next;
	}
    
	/**Класс для хранения информации о всплывающей подсказке над участком текста.*/
	public static class TextToolTip {
		/**Текст в подсказке*/
		public final String text;
		/**Позиция начала выделения*/
		public final int start;
		/**Позиция конца выделения*/
		public final int end;

		/**
		 * Создает всплывающую подсказку.
		 * @param s начальная позиция
		 * @param e конечная позиция
		 * @param t текст подсказки
		 */
		public TextToolTip(int s, int e, String t) {
			start = s;
			end = e;
			if(t.contains("<html>"))
				text = t.replace("<html>", "").replace("</html>", "");
			else
				text = kerlib.tools.escape(t);
		}
	}
    
    
    
    
    /**
     * Применяет стиль к тексту по позициям начала и конца.
     * @param startpos начальная позиция
     * @param endPos конечная позиция
     * @param style функция построения стиля
     */
    public void selectTextByPos(int startpos, int endPos, java.util.function.Function<Style,Style> style){
        selectText(startpos, endPos-startpos,style.apply(new Style()));
    }
    
    /**
     * Применяет стиль к тексту по позициям начала и конца.
     * @param startpos начальная позиция
     * @param endPos конечная позиция
     * @param style стиль для применения
     */
    public void selectTextByPos(int startpos, int endPos, Style style){
        selectText(startpos, endPos-startpos,style);
    }
    
    /**
     * Применяет стиль к тексту по начальной позиции и длине.
     * @param startpos начальная позиция
     * @param lenght длина участка текста
     * @param style функция построения стиля
     */
    public void selectText(int startpos, int lenght, java.util.function.Function<Style,Style> style){
        selectText(startpos, lenght,style.apply(new Style()));
    }
    
	/**
	 * Применяет стиль к участку текста.
	 * @param startpos начальная позиция
	 * @param lenght длина участка текста
	 * @param style стиль для применения
	 * @throws IllegalArgumentException если длина отрицательная
	 */
	public void selectText(int startpos, int lenght, Style style){
		if(lenght < 0){
			throw new IllegalArgumentException("Длина не может быть отрицательной!!!");
		}
		final var doc = this.getStyledDocument();
		final var sas = new SimpleAttributeSet();
		
        var unset = style.set(sas);
        
		doc.setCharacterAttributes(startpos, lenght, sas, false);
        
        unset.accept(sas);
        
		final var end = startpos + lenght;
		doc.setCharacterAttributes(end,0, sas, false);
	}
    
	/**
	 * Сбрасывает все стили форматирования текста.
	 */
	public void resetSelect(){
		final var doc = this.getStyledDocument();
		final var sas = new SimpleAttributeSet();
		doc.setCharacterAttributes(0, Integer.MAX_VALUE, sas, true);
	}
    /**Удаляет все всплывающие подсказки.*/
    public void clearToolTip(){toolTips.clear();}
	
    /**
     * Добавляет всплывающую подсказку к участку текста.
     * @param startpos начальная позиция
     * @param lenght длина участка
     * @param t текст подсказки
     */
    public void selectToolTip(int startpos, int lenght, String t){
        selectToolTipByPos(startpos, startpos+lenght, t, (Style)null);
    }
    
    /**
     * Добавляет всплывающую подсказку к участку текста со стилем.
     * @param startpos начальная позиция
     * @param lenght длина участка
     * @param t текст подсказки
     * @param style стиль выделения
     */
    public void selectToolTip(int startpos, int lenght, String t, Style style){
        selectToolTipByPos(startpos, startpos+lenght, t, style);
    }
    
    /**
     * Добавляет всплывающую подсказку к участку текста со стилем.
     * @param startpos начальная позиция
     * @param lenght длина участка
     * @param t текст подсказки
     * @param style функция построения стиля
     */
    public void selectToolTip(int startpos, int lenght, String t, java.util.function.Function<Style,Style> style){
        selectToolTipByPos(startpos, startpos+lenght, t, style == null ? null : style.apply(new Style()));
    }
    
    /**
     * Добавляет всплывающую подсказку по позициям начала и конца.
     * @param startpos начальная позиция
     * @param endPos конечная позиция
     * @param t текст подсказки
     * @param style функция построения стиля
     */
    public void selectToolTipByPos(int startpos, int endPos, String t, java.util.function.Function<Style,Style> style){
        selectToolTipByPos(startpos, endPos, t, style == null ? null : style.apply(new Style()));
    }
    
    /**
     * Добавляет всплывающую подсказку по позициям начала и конца.
     * @param startpos начальная позиция
     * @param endPos конечная позиция
     * @param t текст подсказки
     * @param st стиль выделения
     */
    public void selectToolTipByPos(int startpos, int endPos, String t, Style st){
		if(st != null)
			selectTextByPos(startpos,endPos,st);
		toolTips.add(new TextToolTip(startpos, endPos, t));
	}
    
    @Override
	public String getToolTipText(MouseEvent event) {
		final var c = this.viewToModel2D(event.getPoint());
		final var tooltip = new StringBuilder();
		for(final var e : toolTips){
			if(e.start <= c && c <= e.end){
				if(tooltip.isEmpty()){
					tooltip.append("<html>").append(e.text);
				} else {
					tooltip.append("<br>").append(e.text);
				}
			}
		}
		return tooltip.isEmpty() ? null : tooltip.toString();
	}
	@Override
	public JToolTip createToolTip() {
        JToolTip tip = new JToolTip();
        tip.setComponent(this);
		tip.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 12));
        return tip;
    }
    
	/**Список всех всплывающих подсказок*/
	private final List<TextToolTip> toolTips = new ArrayList<>();
}
