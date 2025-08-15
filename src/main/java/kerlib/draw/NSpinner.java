/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.JSpinner;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;

/**
 *
 * @author zeus
 */
public class NSpinner extends JSpinner {
    /**Класс, показывающий, какое у нас значение*/
    private Class<? extends Number> classNumber;
  
    public NSpinner() {
        this(new SpinnerNumberModel());
    }
    public NSpinner(SpinnerNumberModel model) {
        super(model);
        classNumber = model.getNumber().getClass();
        final var jtf = ((javax.swing.JSpinner.DefaultEditor) this.getEditor()).getTextField();
        jtf.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String text = jtf.getText().replace(",", "");
                int oldCaretPos = jtf.getCaretPosition();
                try {
                    if(Byte.class.isAssignableFrom(classNumber)){
                        NSpinner.this.setValue(Byte.valueOf(text));
                    } else if(Short.class.isAssignableFrom(classNumber)){
                        NSpinner.this.setValue(Short.valueOf(text));
                    } else if(Integer.class.isAssignableFrom(classNumber)){
                        NSpinner.this.setValue(Integer.valueOf(text));
                    } else if(Long.class.isAssignableFrom(classNumber)){
                        NSpinner.this.setValue(Long.valueOf(text));
                    } else if(Float.class.isAssignableFrom(classNumber)){
                        NSpinner.this.setValue(Float.valueOf(text));
                    } else if(Double.class.isAssignableFrom(classNumber)){
                        NSpinner.this.setValue(Double.valueOf(text));
                    } else {
                        throw new IllegalArgumentException("Нет определения для " + classNumber);
                    }
                    jtf.setCaretPosition(oldCaretPos);
                } catch (NumberFormatException ex) {
                    //Not a number in text field -> do nothing
                } catch (java.lang.IllegalArgumentException ex) { //Удалили цифру, каретка уехала
                    jtf.setCaretPosition(oldCaretPos - 1);
                }
            }
        });
    }  

    @Override
    public void setModel(SpinnerModel model) {
        if(model instanceof SpinnerNumberModel m){
            classNumber = m.getNumber().getClass();
            super.setModel(model);
        } else {
            throw new IllegalArgumentException("Может работать только с числовыми моделями!!!");
        }
    }

    @Override
    public Number getValue() {
        return (Number) super.getValue();
    }
}
