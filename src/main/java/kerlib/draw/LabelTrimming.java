/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.awt.EventQueue;
import javax.swing.JComponent;
import javax.swing.JLabel;

/**
 *
 * @author Kerravitarr
 */
public class LabelTrimming extends JLabel {
	private class Adapter extends java.awt.event.ComponentAdapter {
		@Override
		public void componentResized(java.awt.event.ComponentEvent evt) {
			final var width = getMaxSize.get() - 20;
			final var metrics = LabelTrimming.this.getFontMetrics(LabelTrimming.this.getFont());
			final var ptext = allText.replaceAll("\\n", "\\\\n");
			if (metrics.stringWidth(ptext) < width) {
				LabelTrimming.super.setText(ptext);
			} else {
				var length = ptext.length();
				do {
					final var print = ptext.substring(0, length) + "...";
					if (metrics.stringWidth(print) < width) {
						LabelTrimming.super.setText(print);
						return;
					}
					length--;
				} while (length > 0);
				LabelTrimming.super.setText("...");
			}
		}
	}
	/**Функция постранеия подписи с автоматическим масштабированием содержимого
	 * @param text текст
	 * @param perrent родитель, в котором он размещается (нужен для обработки события изменения размера)
	 * @param size функция возврата доступного для подписи размера
	 */
	public LabelTrimming(String text, JComponent perrent, java.util.function.Supplier<Integer> size){
		super();
		java.util.Objects.requireNonNull(text);
		java.util.Objects.requireNonNull(size);
		this.allText = text;
		getMaxSize = size;
		perrent.addComponentListener(adapter);
		EventQueue.invokeLater(() -> adapter.componentResized(null));
	}
	/** @return весь текст, а не только то, что отображается на экране*/
	public String getAllText(){return allText;}
	@Override public void setText(String text){
		this.allText = text; 
		resize();
	}
	/**Обновить размеры, потому что... фиг знает почему..*/
	public void resize(){
		if(adapter != null) 
			adapter.componentResized(null);
	}
	
	
	/**Максимальный текст*/
	private String allText;
	/**Максимальный размер*/
	private final java.util.function.Supplier<Integer> getMaxSize;
	/**Максимальный размер*/
	private Adapter adapter = new Adapter();
}
