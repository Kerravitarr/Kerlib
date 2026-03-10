/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/**
 * Переключатель, созданный специально для работы с набором перечислений
 * @author ilia
 */
public class SingleEnumPanel<VALUE> extends JComboBox {
    ///Которкая запись, отображающаяся в списке
    private record CBValue<VALUE>(VALUE v, SingleEnumPanel<VALUE> toS){@Override public String toString() {return v == null ? "" : toS.val_to_string.apply(v);}}
    
    
    public SingleEnumPanel(){
        autoCompletion = new AutoCompletion(this);
        var com = (JTextField) getEditor().getEditorComponent();
        com.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                if(isCallSetValue) return;
                try{
                    isCallSetValue = true;
                    setValue(com.getText().trim());
                }finally{
                    isCallSetValue = false;
                }
            }
        });
        com.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyChar() == KeyEvent.VK_ENTER) {
                    if(isCallSetValue) return;
                    try{
                        isCallSetValue = true;
                        setValue(com.getText().trim());
                    }finally{
                        isCallSetValue = false;
                    }
                }
            }
        });
        addActionListener(_ -> changeValue());
    }
    ///@param values установить эти значения, как все возможные
    public SingleEnumPanel<VALUE> setValues(Collection<VALUE> values){this.values.clear();this.values.addAll(values);updateALL(true);return this;}
    ///@param val_to_string функция преобразования значения к строке 
    public void setToString(java.util.function.Function<VALUE, String> val_to_string){this.val_to_string = val_to_string;updateALL(false);}
    ///@param str_to_val функция создания нового значения из строки
    public void setToVal(java.util.function.Function<String,VALUE> str_to_val){
        this.str_to_val = str_to_val;
        autoCompletion.setIsNew(str_to_val != null);
    }
    ///Добавляет слушателя событий изменения. Каждый раз, когда изменяется теущее значение, вызывается эта функция
    public void addChangeListener(java.util.function.Consumer<VALUE> listener){
        ch_listeners.add(listener);
    }
    ///@param selected установить эти значения, как текущие выбранные
    public void setSelected(VALUE selected){
        var model = (DefaultComboBoxModel<CBValue<VALUE>>) getModel();
        for (int i = 0; i < model.getSize(); i++) {
            var v = model.getElementAt(i);
            if(Objects.equals(v.v, selected)){
                model.setSelectedItem(v);
                updateALL(false);
                changeValue();
                return;
            }
        }
        if(str_to_val == null){
            throw new IllegalArgumentException("Нельзя добавить объект " + val_to_string.apply(selected));
        }
        values.add(selected);
        updateALL(true);
        changeValue();
    }
    ///@return получить текущие возможные значения
    public List<VALUE> getValues(){return values;}
    ///@return получить текущие выделенные значения
    public VALUE getSelected(){return getSelectedItem() == null ? null : ((CBValue<VALUE>)getSelectedItem()).v;}
    
    ///Обновляет список возможных значений 
    private void updateALL(boolean isLastSelect){
        var select = getSelected();
        var model = new DefaultComboBoxModel<CBValue<VALUE>>(new CBValue[]{new CBValue<>(null, this)});
        var combo_list = values.stream().map(v -> new CBValue<VALUE>(v, this)).toList();
        model.addAll(combo_list);
        setModel(model);
        if(isLastSelect && !combo_list.isEmpty())
            model.setSelectedItem(combo_list.getLast());
        else if(select != null){
            var find = combo_list.stream().filter(p -> Objects.equals(select, p.v)).findAny();
            if(find.isPresent())
                model.setSelectedItem(find.get());
        }
    }
    
    private synchronized void setValue(String value){
        if (value.isBlank()) return;
        VALUE find = null;
        var model = (DefaultComboBoxModel<CBValue<VALUE>>) getModel();
        var size = model.getSize();
        for (int i = 0; i < size; i++) {
            var v = model.getElementAt(i);
            var str = v.toString();
            if(str.equals(value)){
                find = v.v();
            }
        }
        if (find == null) {
            if(str_to_val != null){
                var res = JOptionPane.showConfirmDialog(null,
                        "Желаете добавить слово '"+value+"' к вариантам выбора?",
                        "Новое слово",
                        JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
                if (res != JOptionPane.NO_OPTION) {
                    try{
                        find = str_to_val.apply(value);
                        values.add(find);
                        updateALL(true);
                    }catch(Exception e){}
                } 
            }
        }
        changeValue();
    }
    ///Обзванивает всех слушателей, оповещая их, что у нас что-то изменилось
    private void changeValue(){
        ch_listeners.forEach(l -> l.accept(getSelected()));
    }
    
    
    
    ///Функция преобразования значения в строку
    private java.util.function.Function<VALUE, String> val_to_string = v -> v.toString();
    ///Можно добавлять новые значения? Если да, то мы должны уметь переводить строку в значение
    private java.util.function.Function<String,VALUE> str_to_val;    ///Список всех значений
    ///Список всех значений
    private List<VALUE> values = new ArrayList<>();
    ///Объект, отвечающий за автодополнение селектора
    private final AutoCompletion autoCompletion;
    ///Слушатели событий изменения
    private final List<java.util.function.Consumer<VALUE>> ch_listeners = new ArrayList<>();
    //Флаг, что сохранение уже идёт
    private boolean isCallSetValue = false;
}
