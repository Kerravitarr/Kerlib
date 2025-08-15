/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.awt.Component;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;

/** Панель, которая автоматически распределеяет всех своих детей по одной линии.
 * @author Kerravitarr
 */
public class ILinePanel extends JPanel {
    ///Ориентация панели
    private boolean isHorisontal(){return getLayout() instanceof javax.swing.BoxLayout bl && bl.getAxis() == javax.swing.BoxLayout.X_AXIS;};
	public ILinePanel(){
		this(javax.swing.BoxLayout.X_AXIS);
	}
	public ILinePanel(int axis){
		super();
		setLayout(new javax.swing.BoxLayout(this, axis));
	}
	
	public ILinePanel label(String text){
		return label(l -> l.setText(text));
	}
	public ILinePanel label(java.util.function.Consumer<javax.swing.JLabel> item){
		var element = new javax.swing.JLabel();
		item.accept(element);
		add(element);
		return this;
	}
	public ILinePanel button(String text, ActionListener listener){
		return button(button ->{
            button.setText(text);
            button.addActionListener(listener);
        });
	}
	public ILinePanel button(java.util.function.Consumer<javax.swing.JButton> button){
		var element = new javax.swing.JButton();
		button.accept(element);
		add(element);
		return this;
	}
	public ILinePanel popup(java.util.function.Consumer<IPopupMenu> item){
		var element = new IPopupMenu();
		add(element);
		addMouseListener(new MouseAdapter(){
			@Override
			public void mouseClicked(MouseEvent evt) {
				if(evt.getClickCount() == 1 && evt.getButton() == MouseEvent.BUTTON3){
					element.removeAll();
					item.accept(element);
					element.show((Component)evt.getSource(), evt.getX(), evt.getY());
				}
			}
		});
		return this;
	}
	public ILinePanel textArea(java.util.function.Consumer<javax.swing.JTextArea> text){
		var element = new javax.swing.JTextArea();
        tools.makeUndoable(element);
		element.setWrapStyleWord(true);
		element.setLineWrap(true);
		text.accept(element);
		add(element);
		return this;
	}
	public ILinePanel textField(java.util.function.Consumer<javax.swing.JTextField> text){
		var element = new javax.swing.JTextField();
        tools.makeUndoable(element);
		text.accept(element);
		add(element);
		return this;
	}
	public ILinePanel checkBox(java.util.function.Consumer<javax.swing.JCheckBox> check){
		var element = new javax.swing.JCheckBox();
		check.accept(element);
		add(element);
		return this;
	}
	public ILinePanel panel(java.util.function.Consumer<ILinePanel> panel){
		return panel(javax.swing.BoxLayout.X_AXIS, panel);
	}
	public ILinePanel panel(int axis, java.util.function.Consumer<ILinePanel> panel){
		var element = new ILinePanel(axis);
		add(element);
		panel.accept(element);
		return this;
	}
	public ILinePanel panel(int axis){
		var element = new ILinePanel(axis);
		add(element);
        return element;
	}
	public ILinePanel panel(){return panel(javax.swing.BoxLayout.X_AXIS);}
	public ILinePanel filler(){
        filler(isHorisontal());
		return this;
	}
	public ILinePanel filler(boolean isHorisontal){
		var element = isHorisontal ? 
				new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0))
				:
				new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
		add(element);
		return this;
	}
	public ILinePanel radioButton(java.util.function.Consumer<java.util.function.Supplier<javax.swing.JRadioButton>> group){
        // create a button group 
        var bg = new javax.swing.ButtonGroup();
		group.accept(() ->{
			var button = new javax.swing.JRadioButton();
			bg.add(button);
			add(button);
			return button;
		});
		return this;
	}
	/**Добавляет разделитель для панели
	 * @return текущий объект
	 */
	public ILinePanel separator(){
		add(new javax.swing.JSeparator(javax.swing.SwingConstants.HORIZONTAL));
		return this;
	}
	public ILinePanel spinner(Number value, Comparable<?> minimum, Comparable<?> maximum, Number stepSize, java.util.function.Consumer<Number> edit){
		var element = spinner(value, minimum, maximum, stepSize);
        element.addChangeListener((_) -> edit.accept((Number)element.getValue()));
		return this;
	}
	public javax.swing.JSpinner spinner(Number value, Comparable<?> minimum, Comparable<?> maximum, Number stepSize){
		var element = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(value, minimum, maximum, stepSize));
		final var jtf = ((javax.swing.JSpinner.DefaultEditor) element.getEditor()).getTextField();
        jtf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = jtf.getText().replace(",", "");
                int oldCaretPos = jtf.getCaretPosition();
                try {
                    var newValue = Double.valueOf(text);
                    element.setValue(kerlib.tools.unbox(value.getClass(), newValue));
                    jtf.setCaretPosition(oldCaretPos);
                } catch (NumberFormatException ex) {
                    //Not a number in text field -> do nothing
                } catch (java.lang.IllegalArgumentException ex) { //Удалили цифру, каретка уехала
                    jtf.setCaretPosition(oldCaretPos - 1);
                }
            }
        });
		add(element);
		return element;
	}
    
	public <T> ILinePanel сomboBox(java.util.List<T> values,T select, java.util.function.Function<T,String> to_string, java.util.function.Consumer<T> edit){
		var cb = new javax.swing.JComboBox();
        class rec{
            T o;
            public rec(T i){o = i;}
            @Override public String toString(){return to_string.apply(o);}
        }
        cb.setModel(new DefaultComboBoxModel(values.stream().map(rec::new).toArray()));
        cb.addActionListener(l -> edit.accept(((rec)cb.getSelectedItem()).o));
        cb.setSelectedIndex(java.util.stream.IntStream.range(0, values.size()).filter(i -> java.util.Objects.equals(values.get(i), select)).findAny().getAsInt());
		add(cb);
		return this;
	}
    
    
    ///Добавляет слушателя на изменение размеров компонента
    ///@param resized слушатель события изменения размера
    ///@return текущую панель
    public ILinePanel componentResized(java.util.function.Consumer<ComponentEvent> resized){
        addComponentListener(new ComponentAdapter(){
            @Override
            public void componentResized(ComponentEvent e) {
                resized.accept(e);
            }
        });
        return this;
    }
}
