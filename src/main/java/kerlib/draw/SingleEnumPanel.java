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
        AutoCompletion.enable(this,true);
        final var com = (JTextField) getEditor().getEditorComponent();
        com.addFocusListener(new FocusAdapter() {
            public void focusLost(FocusEvent e) {
                setValue(com.getText().trim());
            }
        });
        com.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                if (event.getKeyChar() == KeyEvent.VK_ENTER) {
                    setValue(com.getText().trim());
                }
            }
        });
    }
    ///@param values установить эти значения, как все возможные
    public void setValues(Collection<VALUE> values){this.values.clear();this.values.addAll(values);updateALL();}
    ///@return получить текущие возможные значения
    public List<VALUE> getValues(){return values;}
    
    ///Обновляет список возможных значений 
    private void updateALL(){
        var model = new DefaultComboBoxModel<CBValue<VALUE>>(new CBValue[]{new CBValue<>(null, this)});
        model.addAll(values.stream().map(v -> new CBValue<VALUE>(v, this)).toList());
        setModel(model);
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
                final var res = JOptionPane.showConfirmDialog(null,
                        "Желаете добавить слово '"+value+"' к вариантам выбора?",
                        "Новое слово",
                        JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);
                if (res == JOptionPane.NO_OPTION) {
                    return;
                } else {
                    try{
                        find = str_to_val.apply(value);
                        values.add(find);
                        updateALL();
                    }catch(Exception e){
                        return;
                    }
                } 
            } else {
                return;
            }
        }
        var ffind = find;
    }
    
    
    
    ///Функция преобразования значения в строку
    private java.util.function.Function<VALUE, String> val_to_string = v -> v.toString();
    ///Можно добавлять новые значения? Если да, то мы должны уметь переводить строку в значение
    private java.util.function.Function<String,VALUE> str_to_val;    ///Список всех значений
    ///Список всех значений
    private List<VALUE> values = new ArrayList<>();
}
