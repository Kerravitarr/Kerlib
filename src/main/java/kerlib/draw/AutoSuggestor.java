/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.awt.event.KeyEvent;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JToolTip;
import javax.swing.Popup;
import javax.swing.PopupFactory;
import javax.swing.text.BadLocationException;

/**
 *Класс автодоплнения к текстовому полю
 * При вводе пользователем данных, всегда готов предоставить варианты для продолжения ввода
 * @author Kerravitarr
 */
public class AutoSuggestor {
	
	/**Поле, в котором мы будем работать*/
	private final javax.swing.JTextPane textPane;
	
	/**Менюшка с выбором автовставки*/
	private  Popup popup = null;
	/**А это всплывающая подсказка*/
	private final JToolTip toolTip = new JToolTip();
	/**Номер подчёркнутой линии*/
	private int numHighlight = 0;
	/**Выбранный текст*/
	private String selectedText = "";
	/**Варианты подсказок*/
	private Collection<String> variants;
	/**Подключатель. нужно чтобы завершить работу*/
	private final java.awt.event.KeyAdapter adapter; 
	/**Мы открыты? Мы работаем?*/
	private boolean _isOpen = false;
	
	public AutoSuggestor(javax.swing.JTextPane textPane, Collection<String> variants){
		this.textPane = textPane;
		variants(variants);
		adapter = new java.awt.event.KeyAdapter() {
			java.awt.event.KeyEvent last;
			@Override public void keyPressed(java.awt.event.KeyEvent evt) {keyListener(evt, false);}
			@Override public void keyReleased(java.awt.event.KeyEvent evt) {keyListener(evt, true);}
			@Override public void keyTyped(java.awt.event.KeyEvent evt) {
				if(last.isConsumed()) evt.consume();
			}

			private void keyListener(KeyEvent evt, boolean isReleased) {
				last = evt;
				try {
					AutoSuggestor.this.keyListener(evt,isReleased);
				} catch (BadLocationException ex) {
					Logger.getLogger(AutoSuggestor.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		};
		open();
	}
	/**Закрывает сессию работы с панелью*/
	public void close(){
		if(!_isOpen) return;
		_isOpen = false;
		this.textPane.removeKeyListener(adapter);
	}
	/**Открывает сессию работы с панелью*/
	public void open(){
		if(_isOpen) return;
		_isOpen = true;
		this.textPane.addKeyListener(adapter);
	}
    ///@return Все возможные подсказки, которые может выдать программа
    public Collection<String> variants(){return variants;}
    ///@param v Все возможные подсказки, которые может выдать программа
    public void variants(Collection<String> v){variants = v;}
    
	private void keyListener(KeyEvent evt, boolean isReleased) throws BadLocationException {
   		if(popup == null && isActivated(evt)){
			numHighlight = 0;
			updateToolTip();
			evt.consume();
		} else if(isActivated(evt)){
			evt.consume();
		} else if(popup != null){
			switch (evt.getKeyCode()) {
				case KeyEvent.VK_ENTER -> {
					final var word = getWord();
					final var pos = this.textPane.getCaretPosition() - word.length();
					this.textPane.setCaretPosition(pos);
					final var doc = this.textPane.getStyledDocument();
					doc.remove(pos, word.length());
					doc.insertString(pos, selectedText, null);
					this.textPane.setCaretPosition(pos + selectedText.length());
					closeTooltip();
					evt.consume();
					return;
				}
				case KeyEvent.VK_SPACE, KeyEvent.VK_ESCAPE, KeyEvent.VK_TAB -> {
					closeTooltip();
					evt.consume();
					return;
				}
				case KeyEvent.VK_DOWN -> {
					if(!isReleased) numHighlight++;
					evt.consume();
				}
				case KeyEvent.VK_UP -> {
					if(!isReleased) numHighlight--;
					evt.consume();
				}
			}
			updateToolTip();
		}
	}
	/**Возвращает все подходящие строки*/
	private List<String> find(){
		final var word = getWord();
		return variants().stream().filter(variant -> word.isEmpty() || variant.toLowerCase().contains(word.toLowerCase())).toList();
	}
	private void updateToolTip() throws BadLocationException{
		final var find = find();
		if(find.isEmpty()){
			closeTooltip();
			return;
		}
		String text = "<html>";
		numHighlight = (numHighlight = numHighlight % find.size()) < 0 ? numHighlight + find.size() : numHighlight;
		selectedText = find.get(numHighlight);
		
		final var word = getWord();
		for(var i = 0 ; i < find.size(); i++){
			final var variant = find.get(i);
			int startIndex = variant.toLowerCase().indexOf(word.toLowerCase());
			int endIndex = startIndex + word.length();
			if(i == numHighlight) text += "<u>";
			text += variant.substring(0,startIndex);
			text += "<b>" + variant.substring(startIndex, endIndex) + "</b>";
			if(endIndex < variant.length()) {
				text += variant.substring(endIndex);
			}
			if(i == numHighlight) text += "</u>";
			text += "<br>";
		}
		text += "</html>";
		toolTip.setTipText(text);
		final var posCaret = textPane.modelToView2D(textPane.getCaretPosition());
		final var panelPos = textPane.getLocationOnScreen();
		if (popup != null) popup.hide();
		popup = PopupFactory.getSharedInstance().getPopup(textPane, toolTip, panelPos.x + (int) posCaret.getCenterX(), panelPos.y + (int) posCaret.getCenterY());
		popup.show();
		textPane.repaint();
	}
	private String getWord(){
		final var pos = this.textPane.getCaretPosition();
		final var text_ = this.textPane.getText().substring(0,pos);
		final var spasI = Math.max(text_.lastIndexOf(' '), text_.lastIndexOf('\n'));
		return spasI == -1 ? text_ :  text_.substring(spasI+1);
	}
	/**Проверяет условие активации подсказки. Можно переопределить, чтобы активировать по совему усмотрению
	 * @param evt
	 * @return 
	 */
	protected boolean isActivated(KeyEvent evt) {
		return evt.getKeyCode() == KeyEvent.VK_SPACE && evt.isShiftDown();
	}
	private void closeTooltip(){
		if (popup != null)
			popup.hide();
		popup = null;
		textPane.repaint();
	}
}
