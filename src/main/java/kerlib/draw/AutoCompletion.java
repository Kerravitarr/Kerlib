/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.text.*;

/* Автоматическое дополнение для селектора
 * http://creativecommons.org/licenses/publicdomain/
 */
public class AutoCompletion extends PlainDocument {
    ///Элемент, со всеми значениями
	private JComboBox comboBox;
    ///@return текущая модель
	private ComboBoxModel model(){return comboBox.getModel();};
    ///Непосредственно изменяющий объект
	private JTextComponent editor;
	// flag to indicate if setSelectedItem has been called
	// subsequent calls to remove/insertString should be ignored
	private boolean selecting = false;
    ///Нажали backspace
	private boolean hitBackspace = false;
	private boolean hitBackspaceOnSelection;
	/**Флаг возможности ввода нового значения*/
	private boolean isNew = false;
    ///Слушатель событий клавиатуры
	private KeyListener editorKeyListener;
    //Слушщатель события наведения
	private FocusListener editorFocusListener;
    
    //Слушщатель события изменения в селекторе
	private ActionListener actionListener;
    //Слушщатель события изменения свойств селектора. Нужен, чтобы отслеживать, когда у нас изменился контролёр свойств
	private PropertyChangeListener propertyChangeListener;

	public AutoCompletion(final JComboBox comboBox, boolean isNew) {
        this(comboBox);
        setIsNew(isNew);
    }
	public AutoCompletion(final JComboBox comboBox) {
		this.comboBox = comboBox;
		comboBox.setEditable(true);
		comboBox.addActionListener(actionListener = e -> {
			if (!selecting)
                highlightCompletedText(0);
		});
		comboBox.addPropertyChangeListener(propertyChangeListener = e ->  {
            if (e.getPropertyName().equals("editor")) {
                configureEditor((ComboBoxEditor) e.getNewValue());
            }
		});
		editorKeyListener = new KeyAdapter() {
            @Override
			public void keyPressed(KeyEvent e) {
				if (comboBox.isDisplayable())
					comboBox.setPopupVisible(true);
				hitBackspace = false;
				switch (e.getKeyCode()) {
					case KeyEvent.VK_BACK_SPACE -> {
                        try {
                            hitBackspace = lookupItem(getText(0, editor.getSelectionStart())) != null;
                        } catch (BadLocationException ex) {
                            hitBackspace = true;
                        }
                        hitBackspaceOnSelection = editor.getSelectionStart() != editor.getSelectionEnd();
                    }
					case KeyEvent.VK_DELETE -> { //Эту вообще игнорируем
                        e.consume();
                        comboBox.getToolkit().beep();
                    }
				}
            }
		};
		//Выделить весь цвет при получении фокуса
		editorFocusListener = new FocusAdapter() {
            @Override public void focusGained(FocusEvent e) {highlightCompletedText(0);}
		};
		configureEditor(comboBox.getEditor());
		//Обработать изначально выделенный объект
		var selected = comboBox.getSelectedItem();
		if (selected != null) {
			setText(selected.toString());
		}
		highlightCompletedText(0);
	}
    ///@param isNew можно добавлять новые элементы?
    public void setIsNew(boolean isNew){this.isNew = isNew;}
    ///@param comboBox селектор, у которого включаем функцию
    ///@param isNew можно добавлять новые значения?
	public static void enable(JComboBox comboBox, boolean isNew) {
		var a = new AutoCompletion(comboBox);
        a.setIsNew(isNew);
	}

    @Override
	public void remove(int offs, int len) throws BadLocationException {
		//вернуться немедленно при выборе элемента
		if (selecting) {
			return;
		}
		if (hitBackspace) {
            // пользователь нажал backspace => переместить выделение назад
            // старый элемент продолжает быть выбранным
			if (offs > 0) {
				if (hitBackspaceOnSelection) {
					offs--;
				}
				highlightCompletedText(offs);
			} else {
				// Пользователь нажал клавишу Backspace, когда курсор находился на старте => звуковой сигнал
				if(isNew) setText("");
				else comboBox.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
			}
		} else {
			super.remove(offs, len);
		}
	}

    @Override
	public void insertString(int offs, String str, AttributeSet a) throws BadLocationException {
		//вернуться немедленно при выборе элемента
		if (selecting) {
			return;
		}
		// вставьте строку в документ
		super.insertString(offs, str, a);
		// поиск и выбор соответствующего элемента
		var item = lookupItem(getText(0, getLength()));
		if (item != null) {
			setSelectedItem(item);
		} else if(isNew){
			return; //У нас новое значение допустимо, так что не будем мешать пользователю
		} else {
			// оставить старый элемент выбранным, если нет совпадений
			item = comboBox.getSelectedItem();
			// имитировать отсутствие вставки (позже offs будет увеличиваться на str.length(): выборка не будет продвигаться вперед)
			offs = offs - str.length();
			// предоставить пользователю обратную связь о том, что его ввод получен, но не может быть принят
			comboBox.getToolkit().beep(); // when available use: UIManager.getLookAndFeel().provideErrorFeedback(comboBox);
		}
		setText(item.toString());
		// выберите завершенную часть
		highlightCompletedText(offs + str.length());
	}

    
    ///@param newEditor новый объект, отслеживающий изменения в поле. Именно он получает всех слушателей
	private void configureEditor(ComboBoxEditor newEditor) {
		if (editor != null) {
			editor.removeKeyListener(editorKeyListener);
			editor.removeFocusListener(editorFocusListener);
		}

		if (newEditor != null) {
			editor = (JTextComponent) newEditor.getEditorComponent();
			editor.addKeyListener(editorKeyListener);
			editor.addFocusListener(editorFocusListener);
			editor.setDocument(this);
		}
	}
    ///Сохранить новое значение текста в поле
	private void setText(String text) {
		try {
			// удалить весь текст и вставить завершенную строку
			super.remove(0, getLength());
			super.insertString(0, text, null);
		} catch (BadLocationException e) {
			throw new RuntimeException(e.toString());
		}
	}

	private void highlightCompletedText(int start) {
		editor.setCaretPosition(getLength());
		editor.moveCaretPosition(start);
	}

	private void setSelectedItem(Object item) {
		selecting = true;
		model().setSelectedItem(item);
		selecting = false;
	}

	private Object lookupItem(String pattern) {
		Object selectedItem = model().getSelectedItem();
		// only search for a different item if the currently selected does not match
		if (selectedItem != null && startsWithIgnoreCase(selectedItem.toString(), pattern)) {
			return selectedItem;
		} else {
			// iterate over all items
			for (int i = 0, n = model().getSize(); i < n; i++) {
				Object currentItem = model().getElementAt(i);
				// current item starts with the pattern?
				if (currentItem != null && startsWithIgnoreCase(currentItem.toString(), pattern)) {
					return currentItem;
				}
			}
		}
		// no item starts with the pattern => return null
		return null;
	}

	// checks if str1 starts with str2 - ignores case
	private boolean startsWithIgnoreCase(String str1, String str2) {
		return str1.toUpperCase().startsWith(str2.toUpperCase());
	}
}
