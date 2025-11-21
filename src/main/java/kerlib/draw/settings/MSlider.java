/*
 * The MIT License
 *
 * Copyright 2025 Ilia Pushkin (github.com/Kerravitarr).
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package kerlib.draw.settings;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.InsetsUIResource;
import javax.swing.plaf.basic.BasicGraphicsUtils;


/**
 *Реализация многоэлементной полосы прокрутки.
 * По факту одна полоса с несколькими ползунками
 * @author Ilia Pushkin (github.com/Kerravitarr)
 */
public class MSlider extends javax.swing.JPanel {
    public enum ORIENTATION{
        VERTICAL,
        HORIZONTAL
    }
    
    ///Создаёт слайдер с двумя ручками
    public MSlider() {this(0,100);}
    ///Создаёт слайдер с двумя ручками
    /// @param min минимальное значение слайдера
    /// @param max максимальное значение слайдера
    public MSlider(int min, int max) {this(min,max,min,max);}
    ///Создаёт слайдер с несколькими ручками
    /// @param min минимальное значение слайдера
    /// @param max максимальное значение слайдера
    /// @param values значения для всех ползунков
    public MSlider(int min, int max, int ... values) {this(ORIENTATION.HORIZONTAL,min,max,values);}
    ///Создаёт слайдер с несколькими ручками
    ///@param orientation ориентация слайдера
    /// @param min минимальное значение слайдера
    /// @param max максимальное значение слайдера
    /// @param values значения для всех ползунков
    public MSlider(ORIENTATION orientation, int min, int max, int ... values) {
        this.min = min;
        this.max = max;
        if(max < min || values.length < 1 || !(this.values = Arrays.stream(values).boxed().map(v -> new Thumb(v)).toList()).stream().allMatch(v -> min <= v.value && v.value <= max)){
           throw new IllegalArgumentException("Неверно указаны диапазоны размеров"); 
        }
        this.orientation = orientation;
        insetCache = getInsets();
        focusInsets = (Insets)UIManager.get( "Slider.focusInsets" );
        if (focusInsets == null) focusInsets = new InsetsUIResource(2,2,2,2);
        shadowColor = UIManager.getColor("Slider.shadow");
        highlightColor = UIManager.getColor("Slider.highlight");
        focusColor = UIManager.getColor("Slider.focus");
        initComponents();
        
        LookAndFeel.installProperty(this, "opaque", Boolean.TRUE);
        LookAndFeel.installBorder(this, "Slider.border");
        LookAndFeel.installColorsAndFont(this, "Slider.background",
                                         "Slider.foreground", "Slider.font");
        
        scrollTimer = new Timer( 100, mainComponentListener );
        scrollTimer.setInitialDelay( 300 );
        
        addMouseListener(mainComponentListener);
        addMouseMotionListener(mainComponentListener);
        addFocusListener(mainComponentListener);
        addComponentListener(mainComponentListener);
        addPropertyChangeListener( mainComponentListener );
    }
   
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        recalculateIfInsetsChanged();
        recalculateIfOrientationChanged();
        var clip = g.getClipBounds();

        if ( !clip.intersects(trackRect) && paintTicks)
            calculateGeometry();

        if ( paintTicks && clip.intersects( trackRect ) )
            paintTrack( g );
        if ( paintTicks && clip.intersects( tickRect ) ) 
            paintTicks( g );
        if ( paintLabels && clip.intersects( labelRect ) ) 
            paintLabels( g );
        if ( hasFocus() && clip.intersects( focusRect ) ) {
            g.setColor( focusColor );
            BasicGraphicsUtils.drawDashedRect( g, focusRect.x, focusRect.y, focusRect.width, focusRect.height );
        }
        for (var thumbRect : values) {
            if ( clip.intersects( thumbRect ) )
                paintThumb(thumbRect, g );
        }
    }
    ///@return минимальное значение
    public int minValue(){return java.util.Collections.min(values).value;}
    ///@param newMin новое минимальное значение
    public void minValue(int newMin){
        var findi = 0;
        for (var i = 0; i < values.size(); i++) {
            if(values.get(i).compareTo(values.get(findi)) < 0)
                findi = i;
        }
        value(findi, newMin);
    }
    ///@return максимальное значение
    public int maxValue(){return java.util.Collections.max(values).value;}
    ///@param newMax новое максимальное значение
    public void maxValue(int newMax){
        var findi = 0;
        for (var i = 0; i < values.size(); i++) {
            if(values.get(i).compareTo(values.get(findi)) > 0)
                findi = i;
        }
        value(findi, newMax);
    }
    ///Возвращает значение по индексу
    ///@param i индекс элемента
    ///@return значение этого слайдера
    public int value(int i){return values.get(i).value;}
    ///Сохраняет значение по индексу
    ///@param i индекс элемента
    ///@param newValue значение этого слайдера
    public void value(int i, int newValue){value(values.get(i), newValue);}
    ///Добавляет ChangeListener к ползунку.
    ///
    /// @param l the ChangeListener to add
    /// @see #fireStateChanged
    /// @see #removeChangeListener
    public void addChangeListener(ChangeListener l) {
        listenerList.add(ChangeListener.class, l);
    }
    ///Removes a ChangeListener from the slider.
    ///
    ///@param l the ChangeListener to remove
    ///@see #fireStateChanged
    ///@see #addChangeListener
    public void removeChangeListener(ChangeListener l) {
        listenerList.remove(ChangeListener.class, l);
    }    
    
    ///@return Длину штрихов по умолчанию.
    protected int getTickLength(){return 8;}
    
    ///Изменилось текущее значение
    protected void fireStateChanged(){
        Object[] listeners = listenerList.getListenerList();
        for (int i = listeners.length - 2; i >= 0; i -=2 ) {
            if (listeners[i] == ChangeListener.class) {
                if (changeEvent == null)
                    changeEvent = new ChangeEvent(this);
                ((ChangeListener)listeners[i+1]).stateChanged(changeEvent);
            }
        }
    }
    ///Пересчитать всю геометрию. Все размеры слайдера
    protected void calculateGeometry() {
        calculateFocusRect();
        calculateContentRect();
        calculateThumbSize();
        calculateTrackBuffer();
        calculateTrackRect();
        calculateTickRect();
        calculateLabelRect();
        calculateThumbLocation();
    }
    ///Расчёт прямоугольника фокуса
    protected void calculateFocusRect() {
        focusRect.x = insetCache.left;
        focusRect.y = insetCache.top;
        focusRect.width = getWidth() - (insetCache.left + insetCache.right);
        focusRect.height = getHeight() - (insetCache.top + insetCache.bottom);
    }
    ///Прямоугольник содержимого полосы прокрутки
    protected void calculateContentRect() {
        contentRect.x = focusRect.x + focusInsets.left;
        contentRect.y = focusRect.y + focusInsets.top;
        contentRect.width = focusRect.width - (focusInsets.left + focusInsets.right);
        contentRect.height = focusRect.height - (focusInsets.top + focusInsets.bottom);
    }
    ///Обновляет размеры всех полузнков
    protected void calculateThumbSize() {
        var size = getThumbSize();
        values.forEach(v -> v.setSize(size));
    }
    ///Рассчитываем размер для полоски "пути"
    protected void calculateTrackBuffer() {
        var size = getThumbSize();
        if ( paintLabels && labelTable != null ) {
            var keys = labelTable.keySet();
            var maxKey = keys.iterator().next();
            var minKey = maxKey;
            for (var key : keys) {
                if(key > maxKey) maxKey = key;
                if(key < minKey) minKey = key;
            }
            var highLabel = labelTable.get(maxKey);
            var lowLabel = labelTable.get(minKey);
            switch (orientation) {
                case HORIZONTAL -> {
                    trackBuffer = Math.max( highLabel.getBounds().width, lowLabel.getBounds().width ) / 2;
                    trackBuffer = Math.max( trackBuffer, size.width / 2 );
                }
                case VERTICAL -> {
                    trackBuffer = Math.max( highLabel.getBounds().height, lowLabel.getBounds().height ) / 2;
                    trackBuffer = Math.max( trackBuffer, size.height / 2 );
                }
                default ->
                    throw new IllegalArgumentException(String.valueOf(orientation));
            }
        } else {
            trackBuffer = switch (orientation) {
                case HORIZONTAL ->   size.width / 2;
                case VERTICAL -> size.height / 2;
            };
        }
    }
    ///Рассчитываем непосредственно прямоугольник полосы прокрутки
    protected void calculateTrackRect() {
        int centerSpacing;
        var thumbRect = getThumbSize();
        switch (orientation) {
            case HORIZONTAL -> {
                centerSpacing = thumbRect.height;
                if ( paintTicks ) centerSpacing += getTickLength();
                if ( paintLabels ) centerSpacing += getHeightOfTallestLabel();
                trackRect.x = contentRect.x + trackBuffer;
                trackRect.y = contentRect.y + (contentRect.height - centerSpacing - 1)/2;
                trackRect.width = contentRect.width - (trackBuffer * 2);
                trackRect.height = thumbRect.height;
            }
            case VERTICAL -> {
                centerSpacing = thumbRect.width;
                if (getComponentOrientation().isLeftToRight()) {
                    if ( paintTicks ) centerSpacing += getTickLength();
                    if ( paintLabels ) centerSpacing += getWidthOfWidestLabel();
                } else {
                    if ( paintTicks ) centerSpacing -= getTickLength();
                    if ( paintLabels ) centerSpacing -= getWidthOfWidestLabel();
                }
                trackRect.x = contentRect.x + (contentRect.width - centerSpacing - 1)/2;
                trackRect.y = contentRect.y + trackBuffer;
                trackRect.width = thumbRect.width;
                trackRect.height = contentRect.height - (trackBuffer * 2);
            }
            default ->
                throw new IllegalArgumentException(String.valueOf(orientation));
        }
    }
    ///Рассчитывает размер каждого тика
    protected void calculateTickRect() {
        switch (orientation) {
            case HORIZONTAL -> {
                tickRect.x = trackRect.x;
                tickRect.y = trackRect.y + trackRect.height;
                tickRect.width = trackRect.width;
                tickRect.height = (paintTicks) ? getTickLength() : 0;
            }
            case VERTICAL -> {
                tickRect.width = (paintTicks) ? getTickLength() : 0;
                if(isLeftToRight()) {
                    tickRect.x = trackRect.x + trackRect.width;
                } else {
                    tickRect.x = trackRect.x - tickRect.width;
                }
                tickRect.y = trackRect.y;
                tickRect.height = trackRect.height;
            }
            default ->
                throw new IllegalArgumentException(String.valueOf(orientation));
        }
    }
    ///Рассчитывает размер для каждой подписи
    protected void calculateLabelRect() {
        if ( paintLabels ) {
            switch (orientation) {
                case HORIZONTAL -> {
                    labelRect.x = tickRect.x - trackBuffer;
                    labelRect.y = tickRect.y + tickRect.height;
                    labelRect.width = tickRect.width + (trackBuffer * 2);
                    labelRect.height = getHeightOfTallestLabel();
                }
                case VERTICAL -> {
                    if(isLeftToRight()) {
                        labelRect.x = tickRect.x + tickRect.width;
                        labelRect.width = getWidthOfWidestLabel();
                    } else {
                        labelRect.width = getWidthOfWidestLabel();
                        labelRect.x = tickRect.x - labelRect.width;
                    }
                    labelRect.y = tickRect.y - trackBuffer;
                    labelRect.height = tickRect.height + (trackBuffer * 2);
                }
                default ->
                    throw new IllegalArgumentException(String.valueOf(orientation));
            }
        } else {
            switch (orientation) {
                case HORIZONTAL -> {
                    labelRect.x = tickRect.x;
                    labelRect.y = tickRect.y + tickRect.height;
                    labelRect.width = tickRect.width;
                    labelRect.height = 0;
                }
                case VERTICAL -> {
                    if(isLeftToRight())  labelRect.x = tickRect.x + tickRect.width;
                    else  labelRect.x = tickRect.x;
                    labelRect.y = tickRect.y;
                    labelRect.width = 0;
                    labelRect.height = tickRect.height;
                }
                default ->
                    throw new IllegalArgumentException(String.valueOf(orientation));
            }
        }
    }
    ///Рассчитывает положение всех ползунков
    protected void calculateThumbLocation() {
        for (var i = 0; i < values.size(); i++) {
            var value = values.get(i);
            if ( snapToTicks ) {
                int sliderValue = value.value;
                int snappedValue = sliderValue;
                int tickSpacing = getTickSpacing();

                if ( tickSpacing != 0 ) {
                    if ( (sliderValue - min) % tickSpacing != 0 ) {
                        float temp = (float)(sliderValue - min) / (float)tickSpacing;
                        int whichTick = Math.round( temp );
                        
                        if (temp - (int)temp == .5 && sliderValue < value.lastValue)
                          whichTick --;
                        snappedValue = min + (whichTick * tickSpacing);
                    }
                    if( snappedValue != sliderValue ) {
                        value( i, snappedValue );
                    }
                }
            }
            switch (orientation) {
                case HORIZONTAL -> {
                    int valuePosition = xPositionForValue(value.value);
                    value.x = valuePosition - (value.width / 2);
                    value.y = trackRect.y;
                }
                case VERTICAL -> {
                    int valuePosition = yPositionForValue(value.value);
                    value.x = trackRect.x;
                    value.y = valuePosition - (value.height / 2);
                }
                default ->
                    throw new IllegalArgumentException(String.valueOf(orientation));
            }
        }
    }
    ///@return размер одного ползунка
    protected Dimension getThumbSize() {
        return orientation == ORIENTATION.VERTICAL ? new Dimension(20,11) : new Dimension(11,20);
    }
    ///Вычисляет расстояние между засечками
    private int getTickSpacing() {
        if (minorTickSpacing > 0) return minorTickSpacing;
        else if (majorTickSpacing > 0) return majorTickSpacing;
        else  return 0;
    }
    
    ///@return высоту самый высокой метки
    protected int getHeightOfTallestLabel() {return labelTable.values().stream().mapToInt(v -> v.getPreferredSize().height).max().orElse(0);}
    ///@return ширину самой широкой метки
    protected int getWidthOfWidestLabel(){return labelTable.values().stream().mapToInt(v -> v.getPreferredSize().width).max().orElse(0);}
    
    ///Преобразует значение слайдера в значение пикселя на экранe
    /// @param value значение слайдера
    /// @return значение пикселя на экране
    protected int xPositionForValue( int value )    {
        int trackLength = trackRect.width;
        double valueRange = (double)max - (double)min;
        double pixelsPerValue = (double)trackLength / valueRange;
        int trackLeft = trackRect.x;
        int trackRight = trackRect.x + (trackRect.width - 1);
        int xPosition;

        if ( !drawInverted() ) {
            xPosition = trackLeft;
            xPosition += Math.round( pixelsPerValue * ((double)value - min) );
        }
        else {
            xPosition = trackRight;
            xPosition -= Math.round( pixelsPerValue * ((double)value - min) );
        }

        xPosition = Math.max( trackLeft, xPosition );
        xPosition = Math.min( trackRight, xPosition );

        return xPosition;
    }
    ///Преобразует значение слайдера в значение пикселя на экранe
    /// @param value значение слайдера
    /// @return значение пикселя на экране
    protected int yPositionForValue(int value) {
        var trackY = trackRect.y;
        var trackHeight = trackRect.height;
        double valueRange = (double)max - (double)min;
        double pixelsPerValue = (double)trackHeight / valueRange;
        int trackBottom = trackY + (trackHeight - 1);
        int yPosition;

        if ( !drawInverted() ) {
            yPosition = trackY;
            yPosition += Math.round( pixelsPerValue * ((double)max - value ) );
        }
        else {
            yPosition = trackY;
            yPosition += Math.round( pixelsPerValue * ((double)value - min) );
        }

        yPosition = Math.max( trackY, yPosition );
        yPosition = Math.min( trackBottom, yPosition );

        return yPosition;
    }
    ///Преобразует значение пикселя на экране в значение слайдера
    /// @param yPos значение пикселя на экране
    /// @return значение слайдера
    public int valueForYPosition( int yPos ) {
        var minValue = min;
        var maxValue = max;
        var trackLength = trackRect.height;
        var trackTop = trackRect.y;
        var trackBottom = trackRect.y + (trackRect.height - 1);

        if ( yPos <= trackTop ) {
            return drawInverted() ? minValue : maxValue;
        } else if ( yPos >= trackBottom ) {
            return drawInverted() ? maxValue : minValue;
        } else {
            var distanceFromTrackTop = yPos - trackTop;
            var valueRange = (double)maxValue - (double)minValue;
            var valuePerPixel = valueRange / (double)trackLength;
            var valueFromTrackTop = (int)Math.round( distanceFromTrackTop * valuePerPixel );
            return drawInverted() ? minValue + valueFromTrackTop : maxValue - valueFromTrackTop;
        }
    }
    ///Преобразует значение пикселя на экране в значение слайдера
    /// @param xPos значение пикселя на экране
    /// @return значение слайдера
    public int valueForXPosition( int xPos ) {
        var minValue = min;
        var maxValue = max;
        var trackLength = trackRect.width;
        var trackLeft = trackRect.x;
        var trackRight = trackRect.x + (trackRect.width - 1);

        if ( xPos <= trackLeft ) {
            return drawInverted() ? maxValue : minValue;
        } else if ( xPos >= trackRight ) {
            return drawInverted() ? minValue : maxValue;
        } else {
            var distanceFromTrackLeft = xPos - trackLeft;
            var valueRange = (double)maxValue - (double)minValue;
            var valuePerPixel = valueRange / (double)trackLength;
            var valueFromTrackLeft = (int)Math.round( distanceFromTrackLeft * valuePerPixel );

            return drawInverted() ? maxValue - valueFromTrackLeft :  minValue + valueFromTrackLeft;
        }
    } 
    
    ///Сохраняет значение по элементу
    ///@param e элемент
    ///@param newValue значение этого слайдера
    private void value(Thumb e, int newValue){
        newValue = kerlib.tools.betwin(min, newValue, max);
        if(e.compareTo(newValue) != 0){
            e.value = newValue;
            fireStateChanged();
            calculateThumbLocation();
            repaint();
        }
    }
    ///Пересчитывает размеры, если они вдруг изменились
    protected void recalculateIfInsetsChanged() {
        var newInsets = getInsets();
        if ( !newInsets.equals( insetCache ) ) {
            insetCache = newInsets;
            calculateGeometry();
        }
    }
    ///Пересчитывает размеры, если вдуг изменилась ориентация интерфейса
    protected void recalculateIfOrientationChanged() {
        boolean ltr = isLeftToRight();
        if ( ltr!= leftToRightCache ) {
            leftToRightCache = ltr;
            calculateGeometry();
        }
    }
    ///@return Ориентация компонента нормальная? Слева-направо?
    private boolean isLeftToRight(){return getComponentOrientation().isLeftToRight();}
    ///@return true, если надо рисовать наоборот. Если надо 
    protected boolean drawInverted() {
        return switch (orientation) {
            case HORIZONTAL -> isLeftToRight() ? isInverted : !isInverted;
            case VERTICAL -> isInverted;
            default ->
                throw new IllegalArgumentException(String.valueOf(orientation));
        };
    }    
    ///@param g холст, на котором будет нарисована основная полоска ползунка
    public void paintTrack(Graphics g)  {
        var trackBounds = trackRect;
        switch (orientation) {
            case HORIZONTAL -> {
                int cy = (trackBounds.height / 2) - 2;
                int cw = trackBounds.width;

                g.translate(trackBounds.x, trackBounds.y + cy);

                g.setColor(shadowColor);
                g.drawLine(0, 0, cw - 1, 0);
                g.drawLine(0, 1, 0, 2);
                g.setColor(highlightColor);
                g.drawLine(0, 3, cw, 3);
                g.drawLine(cw, 0, cw, 3);
                g.setColor(Color.black);
                g.drawLine(1, 1, cw-2, 1);

                g.translate(-trackBounds.x, -(trackBounds.y + cy));
            }
            case VERTICAL -> {
                int cx = (trackBounds.width / 2) - 2;
                int ch = trackBounds.height;

                g.translate(trackBounds.x + cx, trackBounds.y);

                g.setColor(shadowColor);
                g.drawLine(0, 0, 0, ch - 1);
                g.drawLine(1, 0, 2, 0);
                g.setColor(highlightColor);
                g.drawLine(3, 0, 3, ch);
                g.drawLine(0, ch, 3, ch);
                g.setColor(Color.black);
                g.drawLine(1, 1, 1, ch-2);

                g.translate(-(trackBounds.x + cx), -trackBounds.y);
            }
            default ->
                throw new IllegalArgumentException(String.valueOf(orientation));
        }
    }
    ///@param g холст, на котором будут нарисованы засечки
    public void paintTicks(Graphics g)  {
        Rectangle tickBounds = tickRect;
        g.setColor(Objects.requireNonNullElse(UIManager.getColor("Slider.tickColor"), Color.black));
        switch (orientation) {
            case HORIZONTAL -> {
                g.translate(0, tickBounds.y);

                if (minorTickSpacing > 0) {
                    int value = min;
                    while ( value <= max ) {
                        int xPos = xPositionForValue(value);
                        paintTickForHorizSlider( g, tickBounds, xPos,true );
                        if (Integer.MAX_VALUE - minorTickSpacing < value)
                            break;
                        value += minorTickSpacing;
                    }
                }

                if (majorTickSpacing > 0) {
                    int value = min;
                    while ( value <= max ) {
                        int xPos = xPositionForValue(value);
                        paintTickForHorizSlider( g, tickBounds, xPos, false );
                        if (Integer.MAX_VALUE - majorTickSpacing < value) {
                            break;
                        }
                        value += majorTickSpacing;
                    }
                }

                g.translate( 0, -tickBounds.y);
            }
            case VERTICAL -> {
                g.translate(tickBounds.x, 0);
                if (minorTickSpacing > 0) {
                    int offset = 0;
                    if(!isLeftToRight()) {
                        offset = tickBounds.width - tickBounds.width / 2;
                        g.translate(offset, 0);
                    }
                    int value = min;
                    while (value <= max) {
                        int yPos = yPositionForValue(value);
                        paintTickForVertSlider( g, tickBounds, yPos,true );
                        if (Integer.MAX_VALUE - minorTickSpacing < value)
                            break;
                        value += minorTickSpacing;
                    }
                    if(!isLeftToRight()) 
                        g.translate(-offset, 0);
                }

                if (majorTickSpacing > 0) {
                    if(!isLeftToRight())
                        g.translate(2, 0);
                    int value = min;
                    while (value <= max) {
                        int yPos = yPositionForValue(value);
                        paintTickForVertSlider( g, tickBounds, yPos,false );
                        if (Integer.MAX_VALUE - majorTickSpacing < value) 
                            break;
                        value += majorTickSpacing;
                    }

                    if(!isLeftToRight())
                        g.translate(-2, 0);
                }
                g.translate(-tickBounds.x, 0);
            }
            default ->
                throw new IllegalArgumentException(String.valueOf(orientation));
        }
    }
    ///Рисует засечки на горизонтальном слайдере
    protected void paintTickForHorizSlider( Graphics g, Rectangle tickBounds, int x, boolean isMinor ) {
        g.drawLine( x, 0, x, isMinor ? (tickBounds.height / 2 - 1) : (tickBounds.height - 2));
    }
    ///Рисует засечки на вертикальном слайдере
    protected void paintTickForVertSlider( Graphics g, Rectangle tickBounds, int y, boolean isMinor ) {
        g.drawLine( 0, y, isMinor ? (tickBounds.width / 2 - 1) : (tickBounds.width - 2), y );
    }
    ///@param g холст, на котором будут нарисованы подписи
    public void paintLabels( Graphics g ) {
        var labelBounds = labelRect;

        if ( labelTable != null ) {
            var minValue = min;
            var maxValue = max;
            var enabled = isEnabled();
            for (var en : labelTable.entrySet()) {
                var key = en.getKey();
                
                if (key >= minValue && key <= maxValue) {
                    var label = en.getValue();
                    label.setEnabled(enabled);
                    if (label instanceof javax.swing.JLabel jl) {
                        var icon = label.isEnabled() ? jl.getIcon() : jl.getDisabledIcon();
                        if (icon instanceof javax.swing.ImageIcon iicon) {
                            Toolkit.getDefaultToolkit().checkImage(iicon.getImage(), -1, -1, this);
                        }
                    }
                    switch (orientation) {
                        case HORIZONTAL -> {
                            g.translate( 0, labelBounds.y );
                            paintHorizontalLabel( g, key, label );
                            g.translate( 0, -labelBounds.y );
                        }
                        case VERTICAL -> {
                            int offset = 0;
                            if (!isLeftToRight()) {
                                offset = labelBounds.width -
                                    label.getPreferredSize().width;
                            }
                            g.translate( labelBounds.x + offset, 0 );
                            paintVerticalLabel( g, key, label );
                            g.translate( -labelBounds.x - offset, 0 );
                        }
                        default ->
                            throw new IllegalArgumentException(String.valueOf(orientation));
                    }
                }
            }
        }
    }
    protected void paintHorizontalLabel( Graphics g, int value, Component label ) {
        int labelCenter = xPositionForValue( value );
        int labelLeft = labelCenter - (label.getPreferredSize().width / 2);
        g.translate( labelLeft, 0 );
        label.paint( g );
        g.translate( -labelLeft, 0 );
    }
    protected void paintVerticalLabel( Graphics g, int value, Component label ) {
        int labelCenter = yPositionForValue( value );
        int labelTop = labelCenter - (label.getPreferredSize().height / 2);
        g.translate( 0, labelTop );
        label.paint( g );
        g.translate( 0, -labelTop );
    }
    
    ///@param g холст, на котором будет нарисован ползунок
    protected void paintThumb(Thumb thumb, Graphics g)  {
        int w = thumb.width;
        int h = thumb.height;

        g.translate(thumb.x, thumb.y);
        Rectangle clip = g.getClipBounds();
        g.clipRect(0, 0, w, h);

        if ( isEnabled() )
            g.setColor(getBackground());
        else
            g.setColor(getBackground().darker());

        var paintThumbArrowShape = (Boolean)getClientProperty("Slider.paintThumbArrowShape");
        if ((!paintTicks && paintThumbArrowShape == null) || paintThumbArrowShape == Boolean.FALSE) {
            // "plain" version
            g.fillRect(0, 0, w, h);

            g.setColor(Color.black);
            g.drawLine(0, h-1, w-1, h-1);
            g.drawLine(w-1, 0, w-1, h-1);

            g.setColor(highlightColor);
            g.drawLine(0, 0, 0, h-2);
            g.drawLine(1, 0, w-2, 0);

            g.setColor(shadowColor);
            g.drawLine(1, h-2, w-2, h-2);
            g.drawLine(w-2, 1, w-2, h-3);
        } else {
            switch (orientation) {
                case HORIZONTAL -> {
                    int cw = w / 2;
                    g.fillRect(1, 1, w-3, h-1-cw);
                    Polygon p = new Polygon();
                    p.addPoint(1, h-cw);
                    p.addPoint(cw-1, h-1);
                    p.addPoint(w-2, h-1-cw);
                    g.fillPolygon(p);

                    g.setColor(highlightColor);
                    g.drawLine(0, 0, w-2, 0);
                    g.drawLine(0, 1, 0, h-1-cw);
                    g.drawLine(0, h-cw, cw-1, h-1);

                    g.setColor(Color.black);
                    g.drawLine(w-1, 0, w-1, h-2-cw);
                    g.drawLine(w-1, h-1-cw, w-1-cw, h-1);

                    g.setColor(shadowColor);
                    g.drawLine(w-2, 1, w-2, h-2-cw);
                    g.drawLine(w-2, h-1-cw, w-1-cw, h-2);
                }
                case VERTICAL -> {
                    int cw = h / 2;
                    if(isLeftToRight()) {
                          g.fillRect(1, 1, w-1-cw, h-3);
                          Polygon p = new Polygon();
                          p.addPoint(w-cw-1, 0);
                          p.addPoint(w-1, cw);
                          p.addPoint(w-1-cw, h-2);
                          g.fillPolygon(p);

                          g.setColor(highlightColor);
                          g.drawLine(0, 0, 0, h - 2);                  // left
                          g.drawLine(1, 0, w-1-cw, 0);                 // top
                          g.drawLine(w-cw-1, 0, w-1, cw);              // top slant

                          g.setColor(Color.black);
                          g.drawLine(0, h-1, w-2-cw, h-1);             // bottom
                          g.drawLine(w-1-cw, h-1, w-1, h-1-cw);        // bottom slant

                          g.setColor(shadowColor);
                          g.drawLine(1, h-2, w-2-cw,  h-2 );         // bottom
                          g.drawLine(w-1-cw, h-2, w-2, h-cw-1 );     // bottom slant
                    } else {
                          g.fillRect(5, 1, w-1-cw, h-3);
                          Polygon p = new Polygon();
                          p.addPoint(cw, 0);
                          p.addPoint(0, cw);
                          p.addPoint(cw, h-2);
                          g.fillPolygon(p);

                          g.setColor(highlightColor);
                          g.drawLine(cw-1, 0, w-2, 0);             // top
                          g.drawLine(0, cw, cw, 0);                // top slant

                          g.setColor(Color.black);
                          g.drawLine(0, h-1-cw, cw, h-1 );         // bottom slant
                          g.drawLine(cw, h-1, w-1, h-1);           // bottom

                          g.setColor(shadowColor);
                          g.drawLine(cw, h-2, w-2,  h-2 );         // bottom
                          g.drawLine(w-1, 1, w-1,  h-2 );          // right
                    }
                }
                default ->
                    throw new IllegalArgumentException(String.valueOf(orientation));
            }
        }
        g.setClip(clip);
        g.translate(-thumb.x, -thumb.y);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 26, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents
    ///Ползунок
    protected class Thumb extends java.awt.Rectangle implements Comparable<Thumb>{
        ///Используется для общей функции - сохранить положение ползунка
        private static Rectangle unionRect = new Rectangle();

        ///Текущее значение
        private int value;
        ///Предыдущее значение. Этот элемент нужен, когда осуществляется перестаскивание мышкой - чтобы знать разницу
        private int lastValue;

        public Thumb(int value) {lastValue = this.value = value;}

        @Override
        public int compareTo(Thumb o) {return Integer.compare(value, o.value);}
        public int compareTo(int o) {return Integer.compare(value, o);}
        
        @Override
        public void setLocation(int x, int y)  {
            unionRect.setBounds( this );
            super.setLocation( x, y );

            SwingUtilities.computeUnion( this.x, this.y, this.width, this.height, unionRect );
            MSlider.this.repaint( unionRect.x, unionRect.y, unionRect.width, unionRect.height );
        }
    }
    ///Слушатель события для всех событий
    private class MainListener extends MouseInputAdapter implements ChangeListener, ComponentListener, FocusListener, PropertyChangeListener, ActionListener {
        enum STEP {
            POSITIVE_SCROLL(true),
            NEGATIVE_SCROLL(false),
            MIN_SCROLL(false),
            MAX_SCROLL(true),
            ;
            ///Вращение в сторону увеличения?
            private final boolean isPositive;

            private STEP(boolean isPositive) {
                this.isPositive = isPositive;
            }
            
        }
        ///Смещение
        protected transient int offset;
        ///Текущее положение мыши по X
        protected transient int currentMouseX;
        ///Текущее положение мыши
        protected transient int currentMouseY;
        ///Мы делаем шаги прокрутки с шагом в один блок? Между большими засечками?
        protected boolean useBlockIncrement = false;
        ///Тот ползунок, который мы двигаем
        private Thumb drag = null;
        ///Направление, в которое мы двигаем выбранный ползунок
        private STEP direction = STEP.POSITIVE_SCROLL;
        ///@return У нас сейчас происходит перемещение слайдера?
        public boolean isDragging(){return drag != null;}
        
        ///MouseInputAdapter
        @Override
        public void mouseReleased(MouseEvent e) {
            if (!isEnabled())
                return;
            offset = 0;
            scrollTimer.stop();
            drag = null;
            isAdjusting = false;
            repaint();
        }
        @Override
        public void mousePressed(MouseEvent e) {
            if (!isEnabled())
                return;
            calculateGeometry();
            currentMouseX = e.getX();
            currentMouseY = e.getY();
            if (isRequestFocusEnabled()) {
                requestFocus(FocusEvent.Cause.MOUSE_EVENT);
            }

            //Смотрим, попали ли мы в хоть один из ползунков
            //Если так, то мы начинаем перетаскивать
            var selectThumb = values.stream().filter(v -> v.contains(currentMouseX, currentMouseY)).findAny();
            if (selectThumb.isPresent()) {
                if (UIManager.getBoolean("Slider.onlyLeftMouseButtonDrag") && !SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                var thumbRect = selectThumb.get();
                offset = switch (orientation) {
                    case VERTICAL -> currentMouseY - thumbRect.y;
                    case HORIZONTAL -> currentMouseX - thumbRect.x;
                };
                drag = thumbRect;
                return;
            }

            if (!SwingUtilities.isLeftMouseButton(e)) {
                return;
            }
            isAdjusting = true;

            var sbSize = getSize();
            drag = values.get(0);
            var hip = Math.hypot(currentMouseX - drag.getCenterX(), currentMouseY - drag.getCenterY());
            for (var value : values) {
                var h2 = Math.hypot(currentMouseX - value.getCenterX(), currentMouseY - value.getCenterY());
                if(h2 <= hip){
                    drag = value;
                    hip = h2;
                }
            }
            direction = STEP.POSITIVE_SCROLL;

            switch (orientation) {
                case VERTICAL -> {
                    if ( drag.isEmpty() ) {
                        int scrollbarCenter = sbSize.height / 2;
                        if ( !drawInverted() )
                            direction = (currentMouseY < scrollbarCenter) ? STEP.POSITIVE_SCROLL : STEP.NEGATIVE_SCROLL;
                        else
                            direction = (currentMouseY < scrollbarCenter) ? STEP.NEGATIVE_SCROLL : STEP.POSITIVE_SCROLL;
                    } else if ( !drawInverted() )
                        direction = (currentMouseY < drag.y) ? STEP.POSITIVE_SCROLL : STEP.NEGATIVE_SCROLL;
                    else
                        direction = (currentMouseY < drag.y) ? STEP.NEGATIVE_SCROLL : STEP.POSITIVE_SCROLL;
                }
                case HORIZONTAL -> {
                    if ( drag.isEmpty() ) {
                        int scrollbarCenter = sbSize.width / 2;
                        if ( !drawInverted() )
                            direction = (currentMouseX < scrollbarCenter) ? STEP.NEGATIVE_SCROLL : STEP.POSITIVE_SCROLL;
                        else
                            direction = (currentMouseX < scrollbarCenter) ? STEP.POSITIVE_SCROLL : STEP.NEGATIVE_SCROLL;
                    } else if ( !drawInverted() )
                        direction = (currentMouseX < drag.x) ? STEP.NEGATIVE_SCROLL : STEP.POSITIVE_SCROLL;
                    else
                        direction = (currentMouseX < drag.x) ? STEP.POSITIVE_SCROLL : STEP.NEGATIVE_SCROLL;
                }
            }
            if (shouldScroll(direction,drag)) {
                scrollByBlock(direction,drag);
            }
            if (shouldScroll(direction,drag)) {
                scrollTimer.stop();
                scrollTimer.restart();
            }
        }    
        @Override
        public void mouseDragged(MouseEvent e) {
            int thumbMiddle;
            if (!isEnabled()) {
                return;
            }
            currentMouseX = e.getX();
            currentMouseY = e.getY();

            if (drag == null)
                return;
            isAdjusting = true;
            
            switch (orientation) {
                case VERTICAL -> {
                    int halfThumbHeight = drag.height / 2;
                    int thumbTop = e.getY() - offset;
                    int trackTop = trackRect.y;
                    int trackBottom = trackRect.y + (trackRect.height - 1);
                    int vMax = yPositionForValue(max);

                    if (drawInverted()) trackBottom = vMax;
                    else trackTop = vMax;
                    thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                    thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                    drag.setLocation(drag.x,thumbTop);

                    thumbMiddle = thumbTop + halfThumbHeight;
                    value(drag, valueForYPosition( thumbMiddle ) );
                }
                case HORIZONTAL -> {
                    int halfThumbWidth = drag.width / 2;
                    int thumbLeft = e.getX() - offset;
                    int trackLeft = trackRect.x;
                    int trackRight = trackRect.x + (trackRect.width - 1);
                    int hMax = xPositionForValue(max);

                    if (drawInverted()) trackLeft = hMax;
                    else  trackRight = hMax;
                    thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                    thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                    drag.setLocation(thumbLeft,drag.y);

                    thumbMiddle = thumbLeft + halfThumbWidth;
                    value(drag,valueForXPosition(thumbMiddle));
                }
            }
        }
        
        ///ActionListener
        @Override
        public void actionPerformed(ActionEvent e) {
            if(drag == null){
                ((Timer)e.getSource()).stop();
                return;
            }
            if (useBlockIncrement) {
                scrollByBlock(direction,drag);
            } else {
                scrollByUnit(direction,drag);
            }
            if (!shouldScroll(direction,drag)) {
                ((Timer)e.getSource()).stop();
            }
        }
        
        ///ChangeListener
        @Override
        public void stateChanged(ChangeEvent e) {
            if (!mainComponentListener.isDragging()) {
                calculateThumbLocation();
                MSlider.this.repaint();
            }
            for (var value : values)
                value.lastValue = value.value;
        }
        
        // Component Handler
        @Override public void componentHidden(ComponentEvent e) { }
        @Override public void componentMoved(ComponentEvent e) { }
        @Override public void componentResized(ComponentEvent e) {
            calculateGeometry();
            MSlider.this.repaint();
        }
        @Override public void componentShown(ComponentEvent e) { }
        
        // Focus Handler
        @Override public void focusGained(FocusEvent e) { MSlider.this.repaint(); }
        @Override public void focusLost(FocusEvent e) { MSlider.this.repaint(); }
        
        // Property Change Handler
        @Override public void propertyChange(PropertyChangeEvent e) {
            String propertyName = e.getPropertyName();
            /*if (propertyName == "orientation" ||
                    propertyName == "inverted" ||
                    propertyName == "labelTable" ||
                    propertyName == "majorTickSpacing" ||
                    propertyName == "minorTickSpacing" ||
                    propertyName == "paintTicks" ||
                    propertyName == "paintTrack" ||
                    propertyName == "font" ||
                    SwingUtilities2.isScaleChanged(e) ||
                    propertyName == "paintLabels" ||
                    propertyName == "Slider.paintThumbArrowShape") {
                checkedLabelBaselines = false;
                calculateGeometry();
                repaint();
            } else if (propertyName == "componentOrientation") {
                calculateGeometry();
                slider.repaint();
                InputMap km = getInputMap(JComponent.WHEN_FOCUSED, slider);
                SwingUtilities.replaceUIInputMap(slider,
                    JComponent.WHEN_FOCUSED, km);
            } else if (propertyName == "model") {
                ((BoundedRangeModel)e.getOldValue()).removeChangeListener(
                    changeListener);
                ((BoundedRangeModel)e.getNewValue()).addChangeListener(
                    changeListener);
                calculateThumbLocation();
                repaint();
            }*/
        }
        
        ///Проверяет - можно ли провернуть слайдер на указанное значение
        ///@param direction куда вращаем
        ///@return true, если надо провернуть
        public boolean shouldScroll(STEP direction, Thumb thumb) {
            switch (orientation) {
                case VERTICAL -> {
                    if (drawInverted() ? !direction.isPositive : direction.isPositive) {
                        if (thumb.y  <= currentMouseY) 
                            return false;
                    } else if (thumb.y + thumb.height >= currentMouseY) {
                        return false;
                    }
                }
                case HORIZONTAL -> {
                    if (drawInverted() ? !direction.isPositive : direction.isPositive) {
                        if (thumb.x + thumb.width  >= currentMouseX) {
                            return false;
                        }
                    }
                    else if (thumb.x <= currentMouseX) {
                        return false;
                    }
                }
                default ->
                    throw new IllegalArgumentException(String.valueOf(orientation));
            }
            if (direction.isPositive && thumb.value >= max) {
                return false;
            } else if (!direction.isPositive && thumb.value <= min) {
                return false;
            }
            return true;
        }
        
        ///Изменить значения слайдера на большой шаг
        /// @param direction направление
        /// @param thumb кого именно изменяем
        private void scrollByBlock(MainListener.STEP direction, Thumb thumb)    {
            synchronized(this)    {
                int blockIncrement = (max - min) / 10;
                if (blockIncrement == 0) {
                    blockIncrement = 1;
                }
                int tickSpacing = getTickSpacing();
                if (snapToTicks) {
                    if (blockIncrement < tickSpacing)
                        blockIncrement = tickSpacing;
                } else {
                    if (tickSpacing > 0)
                        blockIncrement = tickSpacing;
                }
                int delta = blockIncrement * (direction.isPositive ? 1 : -1);
                value(thumb,thumb.value + delta);
            }
        } 
        ///Изменить значения слайдера на малый шаг
        /// @param direction направление
        /// @param thumb кого именно изменяем  
        private void scrollByUnit(MainListener.STEP direction, Thumb thumb) {
            synchronized(this)    {
                int delta = (direction.isPositive ? 1 : -1);

                if (snapToTicks) {
                    delta *= getTickSpacing();
                }
                value(thumb,thumb.value + delta);
            }
        }
    }
    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    ///Минимальное значение слайдера
    protected int min;
    ///У нас может быть несколько слайдеров. Поэтому тут список всех слайдеров
    protected List<Thumb> values = new ArrayList<>();
    ///Максимальное значение слайдера
    protected int max;
    ///Округлять, подводить ползунки под ближайшую засечку на оси
    private boolean snapToTicks = false;
    ///Инвертировать слайдер? Инвертировать местами "выделенную" часть и свободную?
    private boolean isInverted = false;
    ///Это особенный флаг. Его назначение заключается в том, что если он true, то фиксировать изменения не надо.
    ///Это сделано для того, чтобы при перемещении элемента, элемент не генерировал сотню сообщений
    private boolean isAdjusting = false;
    ///Число значений между основными делениями — более крупными делениями, которые разбивают второстепенные деления.
    protected int majorTickSpacing = 0;
    ///Количество значений между второстепенными делениями — меньшими делениями, которые встречаются между основными делениями.
    protected int minorTickSpacing = 0;
    
    ///Надо ли делать подписи?
    protected boolean paintLabels = true;
    ///Надо ли делать засечки на оси?
    protected boolean paintTicks = true;
    ///Список подписей, если они уникальны
    private Map<Integer,? extends Component> labelTable = null;
    
    ///Таймер, работающий при перемещении
    protected Timer scrollTimer;
    ///Слушатель всех событий компонента
    protected final MainListener mainComponentListener = new MainListener();
    
    
    ///Основной ограничивающий прямоугольник, кэш для осознания изменившихся размеров
    protected Insets insetCache = null;
    ///Куда повёрнут интерфейс. Кэш для осознания изменения параметра
    protected boolean leftToRightCache = true;
    ///Ограничивающий фокус прямоугольник
    protected Insets focusInsets = null;
    
    ///Расстояние, на котором находится трек от стороны управления
    protected int trackBuffer = 0;
    
    ///Прямоугольник, показывающий, что слайдер в фокусе
    protected Rectangle focusRect = new Rectangle();
    ///Прямоугольник с данными, которые надо отобразить
    protected Rectangle contentRect = new Rectangle();
    ///Прямоугольник, определяющий поле с выделенной полосочкой
    protected Rectangle trackRect = new Rectangle();
    ///Прямоугольник для одного тика
    protected Rectangle tickRect = new Rectangle();
    ///Прямоугольник для подписей
    protected Rectangle labelRect = new Rectangle();
    
    
    ///Цвет полосы прокуртки
    private Color shadowColor;
    ///Цвет фона полосы прокрутки
    private Color highlightColor;
    ///Цвет фона после выделения
    private Color focusColor;
    
    //////Ориентация прокрутки
    protected ORIENTATION orientation;
    ///Для каждого экземпляра модели требуется только один <code>ChangeEvent</code>, поскольку единственным (доступным только для чтения)
    ///состоянием события является свойство source. Источником событий, генерируемых здесь, всегда является «this».
    protected transient ChangeEvent changeEvent = null;
}
