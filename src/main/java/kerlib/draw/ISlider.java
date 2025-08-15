package kerlib.draw;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Hashtable;

import javax.swing.JSlider;

/**
 * Мой личный слайдер, который умеет рисовать оснвоные штрихи только там, где есть метки
 */
public class ISlider extends JSlider {
    
    public ISlider(int orientation, int minimum, int maximum, int value,int stepSize, java.util.function.Function<Integer, String> label) {
        super(orientation,minimum, maximum, value);
        var labels = new Hashtable<Integer, javax.swing.JLabel>();
        for (int i = minimum; i < maximum; i+=stepSize) {
            var text = label.apply(i);
            if(text != null){
                labels.put(i, new javax.swing.JLabel(text));
            }
        }
        setLabelTable(labels);
        setPaintTicks(true);
        setPaintLabels(true);

        addChangeListener(_ -> {
            var nval = getValue();
            if(labels.containsKey(nval)){
                setToolTipText(String.format("[%d]: %s", nval, labels.get(nval).getText()));
            } else {
                String min = null;
                String max = null;
                for (int i = minimum; i < maximum; i+=stepSize) {
                    if(labels.containsKey(i) && i < nval)
                        min = labels.get(i).getText();
                    else if(labels.containsKey(i) && i > nval){
                        max = labels.get(i).getText();
                        break;
                    }
                }
                if(min == null && max == null){
                    setToolTipText(String.format("[%d]", nval));
                } else if(min != null && max == null){
                    setToolTipText(String.format("%s←[%d]",min, nval));
                } else if(min == null && max != null){
                    setToolTipText(String.format("[%d]→%s", nval,max));
                } else {
                    setToolTipText(String.format("%s←[%d]→%s",min, nval,max));
                }
            }
        });
    }
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Получаем таблицу меток
        var labelTable = getLabelTable();
        if (labelTable == null) {
            return;
        }
        // Получаем ориентацию слайдера
        var isHorizontal = getOrientation() == JSlider.HORIZONTAL;

        // Получаем размеры слайдера
        var size = getSize();
        var insets = getInsets();

        // Вычисляем координаты начала и конца слайдера
        var tickSize = 24;
        int trackLeft = insets.left + tickSize/2;
        int trackTop = insets.top + tickSize/2;
        int trackRight = size.width - insets.right - tickSize/2;
        int trackBottom = size.height - insets.bottom - tickSize/2;

        // Рисуем главные линии только в местах меток
        g.setColor(Color.red);
        for(var iter = labelTable.keys().asIterator(); iter.hasNext();) {
            int value = (int)iter.next();
            int pos = (int) ((value - getMinimum()) * (isHorizontal ? (trackRight - trackLeft) : (trackBottom - trackTop)) / (getMaximum() - getMinimum()));

            if (isHorizontal) {
                g.drawLine(trackLeft + pos, trackTop + 10, trackLeft + pos, trackTop - 10);
            } else {
                g.drawLine(trackLeft, trackTop + pos, trackLeft + 10, trackTop + pos);
            }
        }

    }

}
