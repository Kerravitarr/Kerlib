package kerlib.draw;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 *
 * @author Kerravitarr
 */
public class IPopupMenu extends JPopupMenu {
	public static class IMenu extends javax.swing.JMenu{
		public IMenu(String text){
			super(text);
		}
		/**Добавляет элемент меню
		 * @param text название элемента
		 * @param l слушатель события нажатия
		 * @return текущий объект
		 */
		public IMenu addItem(String text, java.awt.event.ActionListener l){
			return addItem(i -> i.setText(text), l);
		}
		/**Добавляет элемент меню
		 * @param item элемент
		 * @param l слушатель события нажатия
		 * @return текущий объект
		 */
		public IMenu addItem(java.util.function.Consumer<javax.swing.JMenuItem> item, java.awt.event.ActionListener l){
			var element = new javax.swing.JMenuItem();
			element.addActionListener(l);
			item.accept(add(element));
			return this;
		}
		/**Добавляет элемент меню
		 * @param name Название элемента меню
		 * @param item элемент
		 * @return текущий объект
		 */
		public IMenu addMenu(String name, java.util.function.Consumer<IMenu> item){
			var element = new IMenu(name);
			item.accept((IMenu)add(element));
			return this;
		}
	}
	
	/**Добавляет элемент меню
	 * @param text название элемента
	 * @param l слушатель события нажатия
	 * @return текущий объект
	 */
	public IPopupMenu addItem(String text, java.awt.event.ActionListener l){
		return addItem(i -> i.setText(text), l);
	}
	/**Добавляет элемент меню
	 * @param item элемент
	 * @param l слушатель события нажатия
	 * @return текущий объект
	 */
	public IPopupMenu addItem(java.util.function.Consumer<javax.swing.JMenuItem> item, java.awt.event.ActionListener l){
		var element = new javax.swing.JMenuItem();
		element.addActionListener(l);
		item.accept(add(element));
		return this;
	}
	/**Добавляет элемент меню
	 * @param name Название элемента меню
	 * @param item элемент
	 * @return текущий объект
	 */
	public IPopupMenu addMenu(String name, java.util.function.Consumer<IMenu> item){
		var element = new IMenu(name);
		item.accept((IMenu)add(element));
		return this;
	}
	@Override
    public JMenuItem add(String s) {
		var label = new javax.swing.JLabel(s);
		label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
		label.setAlignmentX(CENTER_ALIGNMENT);
		add(label);
        return null;
    }
	/**Добавляет разделитель для меню
	 * @return текущий объект
	 */
	public IPopupMenu separator(){
		add(new javax.swing.JSeparator());
		return this;
	}
}
