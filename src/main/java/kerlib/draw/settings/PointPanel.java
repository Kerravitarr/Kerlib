/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package kerlib.draw.settings;

import java.awt.Frame;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import javax.swing.WindowConstants;

/**
 *Универсальный набор:
 * Подпись, ползунок, ресет и две ячейки для ввода точки на экране.
 * По идее это общий ползунок для ввода любых двух чисел.
 * Это могут быть координаты, тогда будет Х и У, а может быть ширина и высота. 
 * К сожалению, это прям писец какой большой класс, так что тут альтернативных конструкторов нет. Пожалуйста, для своего удобства наследуйте эту панель и напишите свой конструктор!
 * @author Kerravitarr
 * @param <XNT> тип числа по оси Х
 * @param <YNT> тип числа по оси Y
 */
public class PointPanel<XNT extends Number & Comparable<XNT>, YNT extends Number & Comparable<YNT>> extends AbstractPanel<PPPoint<XNT,YNT>> {	
    /**
	 * Создаёт панельку настройки для ввода двух чисел
	 *
     * @param texter класс, который позволит получить подписи для элементов настройки
     * @param buttoner класс, который позволит обработать кнопки подписей
	 * @param minX   минимальное значение параметра Х
	 * @param defX   значение параметра Х по умолчанию
	 * @param maxX   максимамльное значение параметра Х
	 * @param nowX   текущее значение параметра Х
	 * @param minY   минимальное значение параметра Y
	 * @param defY   значение параметра Y по умолчанию
	 * @param maxY   максимамльное значение параметра Y
	 * @param nowY   текущее значение параметра Y
	 * @param transform   функция-преобразователь чисел в интересующий вас объект. Чтобы всегда можно было на выходе функции получить желаемый тип
     * @param getPanel функция получения панели, над которой будет летать мышка. Если этот параметр задан, то панелька будет иметь дополнительную кнопку "ввод".
     *              Благодаря этой кнопки пользователь сможет выбрать точку на экране
	 * @param meToPoint   когда мышка будет бегать над панелькой из предыдщей фунцкии, она будет генерировать события мыши. Соответственно тут это событие должно преобразоваться
     *              в объект данных
	 * @param list   слушатель, который сработает, когда значение изменится
	 */ 
    public <RET> PointPanel(TextInterface texter, ButtonInterface buttoner, 
            Comparable<XNT> minX, XNT defX, Comparable<XNT> maxX, XNT nowX,
            Comparable<YNT> minY, YNT defY, Comparable<YNT> maxY, YNT nowY, 
            java.util.function.BiFunction<XNT,YNT,RET> transform, 
            java.util.function.Supplier<java.awt.Component> getPanel, 
            java.util.function.Function<MouseEvent,PPPoint<XNT,YNT>> meToPoint, 
            java.util.function.Consumer<RET> list) {
                this(texter, buttoner, 
                    minX, defX, maxX, nowX,(XNT) kerlib.tools.unbox(nowX.getClass(),1), 
                    minY, defY, maxY, nowY,(YNT) kerlib.tools.unbox(nowY.getClass(),1), 
                    transform, getPanel, meToPoint, list);
            }
	/**
	 * Создаёт панельку настройки для ввода двух чисел
	 *
     * @param texter класс, который позволит получить подписи для элементов настройки
     * @param buttoner класс, который позволит обработать кнопки подписей
	 * @param minX   минимальное значение параметра Х
	 * @param defX   значение параметра Х по умолчанию
	 * @param maxX   максимамльное значение параметра Х
	 * @param nowX   текущее значение параметра Х
	 * @param stepX   шаг изменения параметра Х
	 * @param minY   минимальное значение параметра Y
	 * @param defY   значение параметра Y по умолчанию
	 * @param maxY   максимамльное значение параметра Y
	 * @param nowY   текущее значение параметра Y
	 * @param stepY   шаг изменения параметра Y
	 * @param transform   функция-преобразователь чисел в интересующий вас объект. Чтобы всегда можно было на выходе функции получить желаемый тип
     * @param getPanel функция получения панели, над которой будет летать мышка. Если этот параметр задан, то панелька будет иметь дополнительную кнопку "ввод".
     *              Благодаря этой кнопки пользователь сможет выбрать точку на экране
	 * @param meToPoint   когда мышка будет бегать над панелькой из предыдщей фунцкии, она будет генерировать события мыши. Соответственно тут это событие должно преобразоваться
     *              в объект данных
	 * @param list   слушатель, который сработает, когда значение изменится
	 */   
	public <RET> PointPanel(TextInterface texter, ButtonInterface buttoner, 
            Comparable<XNT> minX, XNT defX, Comparable<XNT> maxX, XNT nowX,XNT stepX, 
            Comparable<YNT> minY, YNT defY, Comparable<YNT> maxY, YNT nowY,YNT stepY, 
            java.util.function.BiFunction<XNT,YNT,RET> transform, 
            java.util.function.Supplier<java.awt.Component> getPanel, 
            java.util.function.Function<MouseEvent,PPPoint<XNT,YNT>> meToPoint, 
            java.util.function.Consumer<RET> list) {
		initComponents();
        initLabel(label, texter);
		labelX.setText(texter.text(TextInterface.Key.LABEL_X));
		labelY.setText(texter.text(TextInterface.Key.LABEL_Y));
        initReset(reset, buttoner, texter, new PPPoint<>(defX,defY));
        
        spinnerX.setModel(new javax.swing.SpinnerNumberModel(nowX, minX, maxX, stepX));
        spinnerY.setModel(new javax.swing.SpinnerNumberModel(nowY, minY, maxY, stepY));
        
        if(getPanel == null){
            select.setVisible(false);
        } else {
            select.setPreferredSize(BUT_SIZE);	
            buttoner.make(ButtonInterface.Key.SELECT, select);
            select.setToolTipText(texter.text(TextInterface.Key.SELECT_B_TOOLTIPTEXT));
            select.addActionListener(_ -> {
                final var dialog = new javax.swing.JDialog((Frame)null, "", false);
                dialog.setAlwaysOnTop(true);
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                final var panel = new javax.swing.JPanel();
                final var pointLabel = new javax.swing.JLabel(texter.text(TextInterface.Key.SELECT_LABEL_EMPTY));
                panel.add(pointLabel);
                dialog.add(panel);

                var world = getPanel.get();

                final var clickListener = new java.awt.event.MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent e) {
                        if(e.getButton() == MouseEvent.BUTTON1){
                            value(meToPoint.apply(e));
                        }
                        dialog.dispose();
                    }
                    @Override
                    public void mouseMoved(MouseEvent e) {
                        final var point = meToPoint.apply(e);
                        pointLabel.setText(MessageFormat.format(texter.text(TextInterface.Key.SELECT_LABEL_FORMAT), point.x(),point.y()));
                    }
                };
                world.addMouseListener(clickListener);
                world.addMouseMotionListener(clickListener);
                dialog.addWindowFocusListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowLostFocus(java.awt.event.WindowEvent e) {
                        dialog.setVisible(false);
                        world.removeMouseMotionListener(clickListener);
                        world.removeMouseListener(clickListener);
                    }
                });

                dialog.pack();
                var pos = java.awt.MouseInfo.getPointerInfo().getLocation();
                var screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
                var height = Math.min(dialog.getPreferredSize().height, screenSize.height / 2);
                var width = Math.min(Math.max(400,dialog.getPreferredSize().width), screenSize.width / 2);
                dialog.setBounds(Math.min(pos.x, screenSize.width * 4 / 5 - width), Math.min(pos.y, screenSize.height * 4 / 5 - height), width, height);
                dialog.setVisible(true);
            });
        }
		value(new PPPoint<>(nowX,nowY));
        
		listener = (v) -> list.accept(transform.apply(v.x(), v.y()));
		spinnerX.addChangeListener(_ -> value(new PPPoint(kerlib.tools.unbox(nowX.getClass(),spinnerX.getValue()),kerlib.tools.unbox(nowY.getClass(),spinnerY.getValue()))));
		spinnerY.addChangeListener(_ -> value(new PPPoint(kerlib.tools.unbox(nowX.getClass(),spinnerX.getValue()),kerlib.tools.unbox(nowY.getClass(),spinnerY.getValue()))));
	}

    @Override
	public void value(PPPoint<XNT,YNT>val) {
		if (!val.equals(value)) {
			value = val;
            spinnerX.setValue(val.x());
            spinnerY.setValue(val.y());
            editValue();
		}
	}
	@Override
	public void setEnabled(boolean isEnabled){
		super.setEnabled(isEnabled);
		spinnerX.setEnabled(isEnabled);
		spinnerY.setEnabled(isEnabled);
		reset.setVisible(isEnabled);
        select.setVisible(isEnabled);
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
        labelX = new javax.swing.JLabel();
        spinnerX = new kerlib.draw.NSpinner();
        labelY = new javax.swing.JLabel();
        spinnerY = new kerlib.draw.NSpinner();
        reset = new javax.swing.JButton();
        select = new javax.swing.JButton();

        setMaximumSize(new java.awt.Dimension(2147483647, 40));
        setLayout(new java.awt.BorderLayout());

        label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        label.setText("name");
        label.setAlignmentY(0.0F);
        add(label, java.awt.BorderLayout.NORTH);

        labelX.setText("X:");
        labelX.setAlignmentY(0.0F);
        labelX.setPreferredSize(new java.awt.Dimension(15, 20));

        spinnerX.setModel(new javax.swing.SpinnerNumberModel(0, 0, null, 1));
        spinnerX.setAlignmentX(0.0F);
        spinnerX.setAlignmentY(0.0F);
        spinnerX.setMaximumSize(new java.awt.Dimension(32767, 20));
        spinnerX.setMinimumSize(new java.awt.Dimension(64, 20));
        spinnerX.setPreferredSize(new java.awt.Dimension(64, 20));

        labelY.setText("Y:");
        labelY.setAlignmentY(0.0F);
        labelY.setPreferredSize(new java.awt.Dimension(15, 20));

        spinnerY.setModel(new javax.swing.SpinnerNumberModel(100, 0, null, 1));
        spinnerY.setAlignmentX(0.0F);
        spinnerY.setAlignmentY(0.0F);
        spinnerY.setMaximumSize(new java.awt.Dimension(32767, 20));
        spinnerY.setMinimumSize(new java.awt.Dimension(68, 20));
        spinnerY.setPreferredSize(new java.awt.Dimension(68, 20));

        reset.setText("reset");
        reset.setAlignmentY(0.0F);

        select.setText("select");
        select.setAlignmentY(0.0F);

        javax.swing.GroupLayout resetAndInsertLayout = new javax.swing.GroupLayout(resetAndInsert);
        resetAndInsert.setLayout(resetAndInsertLayout);
        resetAndInsertLayout.setHorizontalGroup(
            resetAndInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(resetAndInsertLayout.createSequentialGroup()
                .addComponent(labelX, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spinnerX, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelY, javax.swing.GroupLayout.PREFERRED_SIZE, 27, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(spinnerY, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(0, 0, 0)
                .addComponent(reset)
                .addGap(0, 0, 0)
                .addComponent(select))
        );
        resetAndInsertLayout.setVerticalGroup(
            resetAndInsertLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelX, javax.swing.GroupLayout.PREFERRED_SIZE, 19, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(spinnerX, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(labelY, javax.swing.GroupLayout.PREFERRED_SIZE, 17, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(spinnerY, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(reset, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(select, javax.swing.GroupLayout.PREFERRED_SIZE, 21, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        add(resetAndInsert, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel label;
    private javax.swing.JLabel labelX;
    private javax.swing.JLabel labelY;
    private javax.swing.JButton reset;
    private javax.swing.JPanel resetAndInsert;
    private javax.swing.JButton select;
    private javax.swing.JSpinner spinnerX;
    private javax.swing.JSpinner spinnerY;
    // End of variables declaration//GEN-END:variables

}
