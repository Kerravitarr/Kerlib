/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package kerlib.draw.settings;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 *  Универсальный набор:
 * Панель для ввода любых чисел.
 *<br>       НАЗВАНИЕ
 *<br> Скрытый_ввод⋮СПИНЕР [RESET]
 * 
 * 
 * @author Kerravitarr
 * @param <NumberT> тип числового значения
 */
public class NumberPanel<NumberT extends Number & Comparable<NumberT>> extends AbstractPanel<NumberT> {			
    ///Создаёт панельку настройки
    ///Если minS или maxS не заданы, то панелька будет выглядеть проще:
    ///ЗНАЧЕНИЕ      [RESET]
    ///Если панелька используется для целочисленных значений:
    ///  и при этом разница maxS - minS > 1, то будет весь комплект
    ///     ЗНАЧЕНИЕ ****-**** [RESET]
    ///  А вот если maxS - minS меньше 1, то слайдера не буедт! Будет опять базовое:
    ///     ЗНАЧЕНИЕ      [RESET]
    ///Наконец, если панелька используется для чисел с плавающей запятой
    ///  Если разница между maxS - minS > 100, 
    ///
    ///@param texter класс, который позволит получить подписи для элементов настройки
    ///@param buttoner класс, который позволит обработать кнопки подписей
    ///@param minS минимальное значение слайдера
    ///@param defVal значение по умолчанию
    ///@param maxS максимальное значение слайдера
    ///@param mi манимально возможное значение
    ///@param nowVal текущее значение
    ///@param ma максимально возможное значение
    ///@param list слушатель, который сработает, когда значение изменится
    public NumberPanel(TextInterface texter,ButtonInterface buttoner, Number minS, NumberT defVal, Number maxS,Integer blockIncrementS, Comparable<NumberT> mi, NumberT nowVal, Comparable<NumberT> ma, NumberT step, java.util.function.Consumer<NumberT> list) {
		java.util.Objects.requireNonNull(nowVal, "Текущее значение обязано быть числом!");
        MainClazz = (Class<NumberT>) nowVal.getClass();
        initComponents();
        initLabel(label, texter);
        initReset(reset, buttoner, texter, defVal);
        valueSpiner.setModel(new javax.swing.SpinnerNumberModel(kerlib.tools.unbox(MainClazz, nowVal.doubleValue() == 0 ? 1 : 0),mi,ma,step));
        if (mi == null && ma == null)       valueSpiner.setToolTipText("V ∈ R");
		else if (mi != null && ma == null)  valueSpiner.setToolTipText("V ≥ " + mi.toString());
		else if (mi == null && ma != null)  valueSpiner.setToolTipText("V ≤ " + ma.toString());
		else                                valueSpiner.setToolTipText("V ∈ [" + mi + "," + ma + "]");
		if(minS == null || maxS == null || !isFloat() && (maxS.longValue() - minS.longValue()) < 2){
			scroll.setVisible(false);
            spinnerUnvisible.setVisible(false);
            valueSpiner.setVisible(true);
		} else {
            valueSpiner.setVisible(false);
            if(isFloat() && (maxS.longValue() - minS.longValue()) < 100){
                scroll.setBlockIncrement(10);
                scroll.setMinimum(0);
                scroll.setMaximum(99);
                var delta = minS.doubleValue() - maxS.doubleValue();
                scroll.addAdjustmentListener(e ->{
                    value(kerlib.tools.unbox(MainClazz,minS.doubleValue() + e.getValue() * delta));
                });
            } else {
                if(blockIncrementS != null)
                    scroll.setBlockIncrement(blockIncrementS);
                scroll.setMinimum(minS.intValue());
                scroll.setMaximum(maxS.intValue());
                scroll.addAdjustmentListener(e -> {
                    value(kerlib.tools.unbox(MainClazz, e.getValue()));
                });
            }
        }
        spinnerUnvisible.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                valueSpiner.setVisible(!valueSpiner.isVisible());
            }
        });
		value(nowVal);
		listener = list;
        valueSpiner.addChangeListener(_ -> {
            scroll.setValue(0);
            value = kerlib.tools.unbox(MainClazz, valueSpiner.getValue());
            editValue();
        });
	}
    @Override
	public void value(NumberT val) {
        if(!isLess(val) && !isMore(val)){
            valueSpiner.setValue(val);
        }
	}
    ///@return true, если мы слайдер для чисел с плавающей запятой
    private boolean isFloat(){
        return Double.class.isAssignableFrom(MainClazz) || Float.class.isAssignableFrom(MainClazz) || double.class.isAssignableFrom(MainClazz) || float.class.isAssignableFrom(MainClazz);
    }
    ///Проверяет, является ли переданное число меньше нижней границы
    private boolean isLess(NumberT V){
        var min = (Comparable<NumberT>)((javax.swing.SpinnerNumberModel)this.valueSpiner.getModel()).getMinimum();
        return min != null && min.compareTo(V) > 0;
    }
    ///Проверяет, является ли переданное число выше верхней границы
    private boolean isMore(NumberT V){
        var min = (Comparable<NumberT>)((javax.swing.SpinnerNumberModel)this.valueSpiner.getModel()).getMaximum();
        return min != null && min.compareTo(V) < 0;
    }

	/** This method is called from within the constructor to
	 * initialize the form.
	 * WARNING: Do NOT modify this code. The content of this method is
	 * always regenerated by the Form Editor.
	 */
	@SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new javax.swing.JLabel();
        resetAndInsert = new javax.swing.JPanel();
        reset = new javax.swing.JButton();
        centralPanel = new javax.swing.JPanel();
        valueSpiner = new javax.swing.JSpinner();
        spinnerUnvisible = new javax.swing.JLabel();
        scroll = new javax.swing.JScrollBar();

        setMaximumSize(new java.awt.Dimension(2147483647, 40));
        setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("name");
        add(label, java.awt.BorderLayout.NORTH);

        resetAndInsert.setLayout(new javax.swing.BoxLayout(resetAndInsert, javax.swing.BoxLayout.LINE_AXIS));

        reset.setText("reset");
        resetAndInsert.add(reset);

        add(resetAndInsert, java.awt.BorderLayout.EAST);

        centralPanel.setLayout(new javax.swing.BoxLayout(centralPanel, javax.swing.BoxLayout.LINE_AXIS));

        valueSpiner.setModel(new javax.swing.SpinnerNumberModel());
        centralPanel.add(valueSpiner);

        spinnerUnvisible.setText("⋮");
        centralPanel.add(spinnerUnvisible);

        scroll.setBlockIncrement(1);
        scroll.setOrientation(javax.swing.JScrollBar.HORIZONTAL);
        scroll.setUnitIncrement(10);
        scroll.setValue(50);
        scroll.setVisibleAmount(0);
        scroll.setAlignmentX(0.0F);
        centralPanel.add(scroll);

        add(centralPanel, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centralPanel;
    private javax.swing.JLabel label;
    private javax.swing.JButton reset;
    private javax.swing.JPanel resetAndInsert;
    private javax.swing.JScrollBar scroll;
    private javax.swing.JLabel spinnerUnvisible;
    private javax.swing.JSpinner valueSpiner;
    // End of variables declaration//GEN-END:variables
    ///Класс, которым мы являемся (кого хотим получить)
    private final Class<NumberT> MainClazz;
}
