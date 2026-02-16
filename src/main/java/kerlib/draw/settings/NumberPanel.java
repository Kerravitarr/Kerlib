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
    ///Модель для прокрутки, которая запрещает ей сообщать об изменении состояния, если это состояние изменено в спинере
    private static class Scrollmodel extends javax.swing.DefaultBoundedRangeModel {
        ///Нужно ли всем сообщать, что значение изменилось?
        private boolean isDespethers = true;
        @Override
        protected void fireStateChanged() {
            if(isDespethers)
                super.fireStateChanged();
        }
    }
           
    ///Создаёт панельку настройки.
    ///
    ///@param texter класс, который позволит получить подписи для элементов настройки
    ///@param buttoner класс, который позволит обработать кнопки подписей
    ///@param defVal значение по умолчанию
    ///@param nowVal текущее значение
    ///@param list слушатель, который сработает, когда значение изменится
    public NumberPanel(TextInterface texter,ButtonInterface buttoner, NumberT defVal, NumberT nowVal, java.util.function.Consumer<NumberT> list) {
        this(texter, buttoner, null, defVal, null, null, null, nowVal, null, null, list);
    }       
    ///Создаёт панельку настройки.
    ///
	/// @param name название настройки, что будет у неё в заголовке
    ///@param defVal значение по умолчанию
    ///@param nowVal текущее значение
    ///@param list слушатель, который сработает, когда значение изменится
    public NumberPanel(String name, NumberT defVal, NumberT nowVal, java.util.function.Consumer<NumberT> list) {
        this(name, null, defVal, null, null, null, nowVal, null, null, list);
    } 
    ///Создаёт панельку настройки.
    ///
    ///@param texter класс, который позволит получить подписи для элементов настройки
    ///@param buttoner класс, который позволит обработать кнопки подписей
    ///@param defVal значение по умолчанию
    ///@param mi манимально возможное значение
    ///@param nowVal текущее значение
    ///@param ma максимально возможное значение
    ///@param list слушатель, который сработает, когда значение изменится
    public NumberPanel(TextInterface texter,ButtonInterface buttoner, NumberT defVal, Comparable<NumberT> mi, NumberT nowVal, Comparable<NumberT> ma, java.util.function.Consumer<NumberT> list) {
        this(texter, buttoner, null, defVal, null, null, mi, nowVal, ma, null, list);
    }
    ///Создаёт панельку настройки.
    ///
	/// @param name название настройки, что будет у неё в заголовке
    ///@param defVal значение по умолчанию
    ///@param mi манимально возможное значение
    ///@param nowVal текущее значение
    ///@param ma максимально возможное значение
    ///@param list слушатель, который сработает, когда значение изменится
    public NumberPanel(String name, NumberT defVal, Comparable<NumberT> mi, NumberT nowVal, Comparable<NumberT> ma, java.util.function.Consumer<NumberT> list) {
        this(name, null, defVal, null, null, mi, nowVal, ma, null, list);
    }
    ///Создаёт панельку настройки.
    ///Если minS или maxS не заданы, то панелька будет выглядеть проще:
    ///ВВОД [RESET]
    ///Если панелька используется для целочисленных значений:
    ///  и при этом разница maxS - minS > 1, то будет весь комплект
    ///     ВВОД ****-**** [RESET]
    ///  А вот если maxS - minS меньше 1, то слайдера не буедт! Будет опять базовое:
    ///     ВВОД      [RESET]
    ///Наконец, если панелька используется для чисел с плавающей запятой
    ///  Если разница между maxS - minS > 100, то всё будет как и задумывалось
    ///     ВВОД ****-**** [RESET]
    ///  Однако, если разница меньше 100, то вместо этого слайдер будет представлять значение
    ///     от 0 до 99 - в процентах оно будет. Таким образом для чисел с плавающей запятой не
    ///     требуется отдельно добавлять размерные коэффициенты!
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
    public NumberPanel(TextInterface texter,ButtonInterface buttoner, Number minS, NumberT defVal, Number maxS, Comparable<NumberT> mi, NumberT nowVal, Comparable<NumberT> ma, java.util.function.Consumer<NumberT> list) {
        this(texter, buttoner, minS, defVal, maxS, null, mi, nowVal, ma, null, list);
    }
    ///Создаёт панельку настройки.
    ///Если minS или maxS не заданы, то панелька будет выглядеть проще:
    ///ВВОД [RESET]
    ///Если панелька используется для целочисленных значений:
    ///  и при этом разница maxS - minS > 1, то будет весь комплект
    ///     ВВОД ****-**** [RESET]
    ///  А вот если maxS - minS меньше 1, то слайдера не буедт! Будет опять базовое:
    ///     ВВОД      [RESET]
    ///Наконец, если панелька используется для чисел с плавающей запятой
    ///  Если разница между maxS - minS > 100, то всё будет как и задумывалось
    ///     ВВОД ****-**** [RESET]
    ///  Однако, если разница меньше 100, то вместо этого слайдер будет представлять значение
    ///     от 0 до 99 - в процентах оно будет. Таким образом для чисел с плавающей запятой не
    ///     требуется отдельно добавлять размерные коэффициенты!
    ///
	/// @param name название настройки, что будет у неё в заголовке
    ///@param minS минимальное значение слайдера
    ///@param defVal значение по умолчанию
    ///@param maxS максимальное значение слайдера
    ///@param mi манимально возможное значение
    ///@param nowVal текущее значение
    ///@param ma максимально возможное значение
    ///@param list слушатель, который сработает, когда значение изменится
    public NumberPanel(String name, Number minS, NumberT defVal, Number maxS, Comparable<NumberT> mi, NumberT nowVal, Comparable<NumberT> ma, java.util.function.Consumer<NumberT> list) {
        this(name, minS, defVal, maxS, null, mi, nowVal, ma, null, list);
    }
    ///Создаёт панельку настройки.
    ///Если minS или maxS не заданы, то панелька будет выглядеть проще:
    ///ВВОД [RESET]
    ///Если панелька используется для целочисленных значений:
    ///  и при этом разница maxS - minS > 1, то будет весь комплект
    ///     ВВОД ****-**** [RESET]
    ///  А вот если maxS - minS меньше 1, то слайдера не буедт! Будет опять базовое:
    ///     ВВОД      [RESET]
    ///Наконец, если панелька используется для чисел с плавающей запятой
    ///  Если разница между maxS - minS >= 100, то всё будет как и задумывалось
    ///     ВВОД ****-**** [RESET]
    ///  Однако, если разница меньше 100, то вместо этого слайдер будет представлять значение
    ///     от 0 до 99 - в процентах оно будет. Таким образом для чисел с плавающей запятой не
    ///     требуется отдельно добавлять размерные коэффициенты!
    ///
	/// @param name название настройки, что будет у неё в заголовке
    ///@param minS минимальное значение слайдера
    ///@param defVal значение по умолчанию
    ///@param maxS максимальное значение слайдера
    ///@param blockIncrementS если maxS и maxS заданы, NumberT - целочисленное, или maxS - minS >= 100, то это поле показывает на сколько будет прыжков перемещаться слайдер за один клик рядом с полосой
    ///@param mi манимально возможное значение
    ///@param nowVal текущее значение
    ///@param ma максимально возможное значение
    ///@param step шаг, с которым изменяется значение у scroll за поворот. По умолчанию, если заданы значения mi и ma, то используется 1/100 от их разницы!
    ///@param list слушатель, который сработает, когда значение изменится
    public NumberPanel(String name, Number minS, NumberT defVal, Number maxS,Integer blockIncrementS, Comparable<NumberT> mi, NumberT nowVal, Comparable<NumberT> ma, NumberT step, java.util.function.Consumer<NumberT> list) {
        this(name,null, null, minS, defVal, maxS, blockIncrementS, mi, nowVal, ma, step, list);
    }
    ///Создаёт панельку настройки.
    ///Если minS или maxS не заданы, то панелька будет выглядеть проще:
    ///ВВОД [RESET]
    ///Если панелька используется для целочисленных значений:
    ///  и при этом разница maxS - minS > 1, то будет весь комплект
    ///     ВВОД ****-**** [RESET]
    ///  А вот если maxS - minS меньше 1, то слайдера не буедт! Будет опять базовое:
    ///     ВВОД      [RESET]
    ///Наконец, если панелька используется для чисел с плавающей запятой
    ///  Если разница между maxS - minS >= 100, то всё будет как и задумывалось
    ///     ВВОД ****-**** [RESET]
    ///  Однако, если разница меньше 100, то вместо этого слайдер будет представлять значение
    ///     от 0 до 99 - в процентах оно будет. Таким образом для чисел с плавающей запятой не
    ///     требуется отдельно добавлять размерные коэффициенты!
    ///
    ///@param texter класс, который позволит получить подписи для элементов настройки
    ///@param buttoner класс, который позволит обработать кнопки подписей
    ///@param minS минимальное значение слайдера
    ///@param defVal значение по умолчанию
    ///@param maxS максимальное значение слайдера
    ///@param blockIncrementS если maxS и maxS заданы, NumberT - целочисленное, или maxS - minS >= 100, то это поле показывает на сколько будет прыжков перемещаться слайдер за один клик рядом с полосой
    ///@param mi манимально возможное значение
    ///@param nowVal текущее значение
    ///@param ma максимально возможное значение
    ///@param step шаг, с которым изменяется значение у scroll за поворот. По умолчанию, если заданы значения mi и ma, то используется 1/100 от их разницы!
    ///@param list слушатель, который сработает, когда значение изменится
    public NumberPanel(TextInterface texter,ButtonInterface buttoner, Number minS, NumberT defVal, Number maxS,Integer blockIncrementS, Comparable<NumberT> mi, NumberT nowVal, Comparable<NumberT> ma, NumberT step, java.util.function.Consumer<NumberT> list) {
        this(null,texter, buttoner, minS, defVal, maxS, blockIncrementS, mi, nowVal, ma, step, list);
    }
    ///Создаёт панельку настройки.
    ///Если minS или maxS не заданы, то панелька будет выглядеть проще:
    ///ВВОД [RESET]
    ///Если панелька используется для целочисленных значений:
    ///  и при этом разница maxS - minS > 1, то будет весь комплект
    ///     ВВОД ****-**** [RESET]
    ///  А вот если maxS - minS меньше 1, то слайдера не буедт! Будет опять базовое:
    ///     ВВОД      [RESET]
    ///Наконец, если панелька используется для чисел с плавающей запятой
    ///  Если разница между maxS - minS >= 100, то всё будет как и задумывалось
    ///     ВВОД ****-**** [RESET]
    ///  Однако, если разница меньше 100, то вместо этого слайдер будет представлять значение
    ///     от 0 до 99 - в процентах оно будет. Таким образом для чисел с плавающей запятой не
    ///     требуется отдельно добавлять размерные коэффициенты!
    ///
    ///@param texter класс, который позволит получить подписи для элементов настройки
    ///@param buttoner класс, который позволит обработать кнопки подписей
    ///@param minS минимальное значение слайдера
    ///@param defVal значение по умолчанию
    ///@param maxS максимальное значение слайдера
    ///@param blockIncrementS если maxS и maxS заданы, NumberT - целочисленное, или maxS - minS >= 100, то это поле показывает на сколько будет прыжков перемещаться слайдер за один клик рядом с полосой
    ///@param mi манимально возможное значение
    ///@param nowVal текущее значение
    ///@param ma максимально возможное значение
    ///@param step шаг, с которым изменяется значение у scroll за поворот. По умолчанию, если заданы значения mi и ma, то используется 1/100 от их разницы!
    ///@param list слушатель, который сработает, когда значение изменится
    private NumberPanel(String name, TextInterface texter,ButtonInterface buttoner, Number minS, NumberT defVal, Number maxS,Integer blockIncrementS, Comparable<NumberT> mi, NumberT nowVal, Comparable<NumberT> ma, NumberT step, java.util.function.Consumer<NumberT> list) {
		java.util.Objects.requireNonNull(nowVal, "Текущее значение обязано быть числом!");
        MainClazz = (Class<NumberT>) nowVal.getClass();
        if(texter == null)
            texter = k -> k == TextInterface.Key.LABEL ? name : null;
        if(buttoner == null)
            buttoner = (k,b) -> b.setText("↻");
        initComponents();
        initLabel(label, texter);
        initReset(reset, buttoner, texter, defVal);
        step = (NumberT)(mi == null || ma == null || !(mi instanceof Number) || !(ma instanceof Number || step != null) ? (step == null ? kerlib.tools.unbox(MainClazz,1) : step) : (kerlib.tools.unbox(MainClazz,(((Number)ma).doubleValue() - ((Number)mi).doubleValue())/100d )));
        try{
            valueSpiner.setModel(new javax.swing.SpinnerNumberModel(nowVal,mi,ma,step));
        } catch(java.lang.IllegalArgumentException ex){
            throw new java.lang.IllegalArgumentException(ex.getMessage() + " in NumberPanel " + name);
        }
        if (mi == null && ma == null)       valueSpiner.setToolTipText("V ∈ R");
		else if (mi != null && ma == null)  valueSpiner.setToolTipText("V ≥ " + mi.toString());
		else if (mi == null && ma != null)  valueSpiner.setToolTipText("V ≤ " + ma.toString());
		else                                valueSpiner.setToolTipText("V ∈ [" + mi + "," + ma + "]");
        var hideScroll = minS == null || maxS == null || !isFloat() && (maxS.longValue() - minS.longValue()) < 2;
        var scrollAsPercent = !hideScroll && (isFloat() && (maxS.longValue() - minS.longValue()) < 100);
        scroll.setModel(new Scrollmodel());
		if(hideScroll){
			scroll.setVisible(false);
            spinnerUnvisible.setVisible(false);
            valueSpiner.setVisible(true);
		} else {
            valueSpiner.setVisible(false);
            if(scrollAsPercent){
                scroll.setBlockIncrement(10);
                scroll.setMinimum(0);
                scroll.setMaximum(99);
                var delta = minS.doubleValue() - maxS.doubleValue();
                scroll.setValue(kerlib.tools.betwin(0, (int) Math.round((nowVal.doubleValue() - minS.doubleValue()) / (minS.doubleValue() - maxS.doubleValue())), 99));
                scroll.addAdjustmentListener(e ->{
                    value(kerlib.tools.unbox(MainClazz,minS.doubleValue() + e.getValue() * delta));
                });
            } else {
                if(blockIncrementS != null)
                    scroll.setBlockIncrement(blockIncrementS);
                scroll.setMinimum(minS.intValue());
                scroll.setMaximum(maxS.intValue());
                scroll.setValue(kerlib.tools.betwin(minS.intValue(), nowVal.intValue(), maxS.intValue()));
                scroll.addAdjustmentListener(e -> {
                    value(kerlib.tools.unbox(MainClazz, e.getValue()));
                });
            }
        }
        spinnerUnvisible.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e) {
                valueSpiner.setVisible(!valueSpiner.isVisible());
                scroll.setVisible(!valueSpiner.isVisible());
                spinnerUnvisible.setText(valueSpiner.isVisible() ? "←" : "→");
            }
        });
		value(nowVal);
		listener = list;
        valueSpiner.addChangeListener(_ -> {
            value = kerlib.tools.unbox(MainClazz, valueSpiner.getValue());
            var a = (Scrollmodel) scroll.getModel();
            a.isDespethers = false;
            if(!hideScroll){
                if(scrollAsPercent)
                    scroll.setValue(kerlib.tools.betwin(0, (int) Math.round((value.doubleValue() - minS.doubleValue()) / (minS.doubleValue() - maxS.doubleValue())), 99));
                else
                    scroll.setValue(kerlib.tools.betwin(minS.intValue(), value.intValue(), maxS.intValue()));
            }
            a.isDespethers = true;
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

        spinnerUnvisible.setText("→");
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
