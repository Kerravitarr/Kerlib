/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.awt.EventQueue;
import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

/**
 *
 * @author Kerravitarr
 */
public class TrimmingTextComponent {
	private class Adapter extends java.awt.event.ComponentAdapter {
		@Override
		public void componentResized(java.awt.event.ComponentEvent evt) {
			final var max = getMaxSize.get();
			if(max == null){
				component.setText(allText);
			} else {
				final var width = max - 20;
				final var metrics = component.getFontMetrics(component.getFont());
				final var ptext = allText.replaceAll("\\n", "\\\\n");
				if (metrics.stringWidth(ptext) < width) {
					component.setText(ptext);
				} else {
					var length = ptext.length();
					do {
						final var print = ptext.substring(0, length) + "...";
						if (metrics.stringWidth(print) < width) {
							component.setText(print);
							return;
						}
						length--;
					} while (length > 0);
					component.setText("...");
				}
				EventQueue.invokeLater(() -> {
					component.revalidate();
					component.repaint();
				});
			}
		}
	}
	
	/**Функция постранеия подписи с автоматическим масштабированием содержимого
	 * @param component объект, в котором будем работать
	 * @param text изначальный текст. Его, если что, всегда можно изменить
	 * @param perrent родитель, в котором он размещается (нужен для обработки события изменения размера)
	 * @param size функция возврата доступного для подписи размера. Если null, то компонент размеры использует максимальные
	 */
	public TrimmingTextComponent(JTextComponent component, String text, JComponent perrent, java.util.function.Supplier<Integer> size){
		super();
		this.allText = text;
		this.component = component;
		getMaxSize = size;
		perrent.addComponentListener(adapter);
		EventQueue.invokeLater(() -> resize());
	}
	
	
	/** @return весь текст, а не только то, что отображается на экране*/
	public String getText(){return allText;}
	/** @param text полный текст, который желаем видеть на экране*/
	public void setText(String text){
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
	private final Adapter adapter = new Adapter();
	/**Объект, трансформации которого мы будем подвергать*/
	private final JTextComponent component;
}
