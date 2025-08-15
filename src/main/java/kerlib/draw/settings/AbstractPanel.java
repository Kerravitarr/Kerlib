/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw.settings;

import java.awt.Dimension;
import kerlib.draw.ILinePanel;

/**Уневерсальная панель настроек
 */
abstract class AbstractPanel<T> extends ILinePanel {
	///Размер кнопок по умолчанию
	protected static final Dimension BUT_SIZE = new Dimension(20,15);
    ///Текущее значение
    protected T value;
	///Слушатель того, что изменилось состяние поля. 
	protected java.util.function.Consumer<T> listener;
    
	///@return текущее значение блока 
	public T value() {return value;}
	///@param newVal новое установленное значение
    public abstract void value(T newVal);
    
    ///Функция, которая должна вызываться, когда изменяется значение
    protected void editValue(){
        if(listener != null)
            listener.accept(value());
    }
    
    ///Сохраняет для подписи всю информацию
    protected static void initLabel(javax.swing.JLabel label, TextInterface texter){
		label.setText(texter.text(TextInterface.Key.LABEL));
		label.setToolTipText(texter.text(TextInterface.Key.TOOLTIPTEXT));
    }
    ///Инициаизирует кнопку сброса
    protected void initReset(javax.swing.JButton reset,ButtonInterface buttoner, TextInterface texter, T defVal) {
		reset.setPreferredSize(BUT_SIZE);	
        buttoner.make(ButtonInterface.Key.RESET, reset);
		reset.addActionListener(e -> value(defVal));
		reset.setToolTipText(texter.text(TextInterface.Key.RESET_B_TOOLTIPTEXT));
    }    
}
