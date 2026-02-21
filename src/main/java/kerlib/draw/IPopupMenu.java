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
            if(l != null)
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
        
        /** @return меню старого образца. Функция не очень умная, но кое что преобразовать может!*/
        public java.awt.Menu toOld(){
            var ret = new java.awt.Menu();
            ret.setLabel(getText());
            for(var e : getMenuComponents())
                transform(ret,e);
            return ret;
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
    /** @return меню старого образца. Функция не очень умная, но кое что преобразовать может!*/
    public java.awt.PopupMenu toOld(){
        var ret = new java.awt.PopupMenu();
        for(var e : getComponents())
            transform(ret,e);
        return ret;
    }
    private static void transform(java.awt.Menu target, java.awt.Component e){
        if(e instanceof javax.swing.JLabel l){
            target.add(l.getText());
        } else if(e instanceof javax.swing.JSeparator){
            target.addSeparator();
        } else if(e instanceof IMenu m){
            target.add(m.toOld());
        } else if(e instanceof javax.swing.JMenuItem i){
            var item = new java.awt.MenuItem(i.getText());
            for(var l : i.getActionListeners())
                item.addActionListener(l);
            target.add(item);
        } else {
            throw new ClassCastException("Нельзя преобразовать элемент " + (e == null ? null : e.getClass()) + " к старой форме");
        }
    }
}
