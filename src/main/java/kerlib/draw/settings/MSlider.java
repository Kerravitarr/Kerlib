///
/// The MIT License
///
/// Copyright 2025 Ilia Pushkin (github.com/Kerravitarr).
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in
/// all copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
/// THE SOFTWARE.
///

package kerlib.draw.settings;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.Rectangle;
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
import javax.swing.BoundedRangeModel;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JSlider;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.plaf.synth.SynthContext;

///
///
/// @author Ilia Pushkin (github.com/Kerravitarr)
public class MSlider extends JSlider {
    public enum ORIENTATION{
        VERTICAL(JSlider.VERTICAL),
        HORIZONTAL(JSlider.HORIZONTAL);
        public final int SW_CONST;

        private ORIENTATION(int SW_CONST) {
            this.SW_CONST = SW_CONST;
        }
        
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
        super(orientation.SW_CONST,min,max,max);
        if(max < min || values.length < 1 || !(this.values = Arrays.stream(values).boxed().map(v -> new Thumb(v)).toList()).stream().allMatch(v -> min <= v.value && v.value <= max)){
           throw new IllegalArgumentException("Неверно указаны диапазоны размеров"); 
        }
        updateUI();
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
    public int value(int i){return values == null ? 0 : values.get(i).value;}
    ///Сохраняет значение по индексу
    ///@param i индекс элемента
    ///@param newValue значение этого слайдера
    public void value(int i, int newValue){value(values.get(i), newValue);}

    @Override public int getValue() {return value(0);}
    @Override public void setValue(int n) {value(0,n);}
    
    @Override public void updateUI() {
        switch (UIManager.getLookAndFeel().getName()) {
            case "Metal" -> setUI(new MMetalSliderUI());
            case "Nimbus" -> setUI(new MSynthSliderUI());
            case "GTK look and feel" -> setUI(new MSynthSliderUI());
            default -> {
                System.out.println(UIManager.getLookAndFeel().getName());
                setUI(new MBasicSliderUI());
            }
        }
        updateLabelUIs();
    }
    
    
    ///Сохраняет значение по элементу
    ///@param e элемент
    ///@param newValue значение этого слайдера
    private void value(Thumb e, int newValue){
        newValue = kerlib.tools.betwin(getMinimum(), newValue, getMaximum());
        if(e.compareTo(newValue) != 0){
            e.value = newValue;
            var m = getModel();
            m.setValue(m.getValue() == m.getMinimum() ? m.getMaximum() : m.getMinimum());
        }
    }
    ///@return Ориентация компонента нормальная? Слева-направо?
    private boolean isLeftToRight(){return getComponentOrientation().isLeftToRight();}
    
 
    ///Один ползунок, элемент, который будет перемещаться по экрану
    protected class Thumb extends java.awt.Rectangle implements Comparable<Thumb>{
        ///Используется для общей функции - сохранить положение ползунка
        private static Rectangle unionRect = new Rectangle();

        ///Текущее значение
        private int value;
        ///Предыдущее значение. Этот элемент нужен, когда осуществляется перестаскивание мышкой - чтобы знать разницу
        private int lastValue;
        ///Активируется, если на компонент навести мышкой
        private boolean isActive;
        ///Активируется, если мышка нажимает на компонент
        private boolean thumbPressed;

        public Thumb(int value) {lastValue = this.value = value;}

        @Override
        public int compareTo(Thumb o) {return Integer.compare(value, o.value);}
        public int compareTo(int o) {return Integer.compare(value, o);}
        
        @Override
        public void setLocation(int x, int y)  {
            if(this.x == x && this.y == y) return;
            unionRect.setBounds( this );
            super.setLocation( x, y );

            SwingUtilities.computeUnion( this.x, this.y, this.width, this.height, unionRect );
            //MSlider.this.repaint( unionRect.x, unionRect.y, unionRect.width, unionRect.height );
            MSlider.this.repaint();
        }
        public void setActive(boolean isActive){
            if(this.isActive != isActive){
                this.isActive = isActive;
                MSlider.this.repaint();
            }
        }

        @Override
        public String toString() {return String.valueOf(value);}
    }
    ///Суперкласс, для объединения всех потомков в один вид
    public interface MultiSliderUI {
        ///@return У нас сейчас происходит перемещение слайдера?
        default boolean isDrag(){return drag() != null;}
        ///@return элемент, который сейчас перемещается
        Thumb drag();
        ///@param thumb элемент, который будет перемещаться
        void drag(Thumb thumb);
        ///@return слайдер, который относится к текущему классу
        MSlider slider();
        ///@return таймер, отвечающий за прокрутку
        javax.swing.Timer scrollTimer();
        ///@return true, если надо изображать элемент зеркально
        boolean drawInverted();
        ///Пересчитать положение всех ползунков
        void calculateThumbLocation();
        ///Пересчитать расположение всех элементов
        void calculateGeometry();
        ///@param dir куда провернуть полосу прокрутки
        void scrollDueToClickInTrack(int dir);
        ///@param dir куда будет вращаться элемет далее
        void setDirection(int dir);
        ///@return прямоугольник, который ограничивает поле перемещения ползунка
        Rectangle trackRect();
        ///Преобразует значение слайдера в значение пикселя на экранe
        /// @param value значение слайдера
        /// @return значение пикселя на экране
        int yPositionForValue(int value);
        ///Преобразует значение пикселя на экране в значение слайдера
        /// @param yPos значение пикселя на экране
        /// @return значение слайдера
        int valueForYPosition(int yPos);
        ///Преобразует значение слайдера в значение пикселя на экранe
        /// @param value значение слайдера
        /// @return значение пикселя на экране
        int xPositionForValue(int value);
        ///Преобразует значение пикселя на экране в значение слайдера
        /// @param xPos значение пикселя на экране
        /// @return значение слайдера
        int valueForXPosition(int xPos);
    }
    ///Базовый менеджер внешнего вида
    protected static class MBasicSliderUI extends javax.swing.plaf.basic.BasicSliderUI implements MultiSliderUI{
        ///Слушатель события для мыши. Новая мышь - новый слушатель
        public static class MTrackListener extends TrackListener {
            ///Базовый элемент, который даст нам доступ ко всем нужным полям
            protected final MultiSliderUI UI;
            public MTrackListener(MultiSliderUI base) {new javax.swing.plaf.basic.BasicSliderUI(null).super(); UI = base;}

            @Override
            public void mouseExited(MouseEvent e) {
                    UI.slider().values.forEach(v -> v.setActive(false));
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                var slider = UI.slider();
                if (!slider.isEnabled())
                    return;
                offset = 0;
                UI.scrollTimer().stop();
                UI.drag(null);
                slider.values.forEach(v -> v.thumbPressed = false);
                slider.setValueIsAdjusting(false);
                slider.repaint();
            }
            @Override
            public void mousePressed(MouseEvent e) {
                var slider = UI.slider();
                if (!slider.isEnabled())
                    return;
                UI.calculateGeometry();
                currentMouseX = e.getX();
                currentMouseY = e.getY();
                if (slider.isRequestFocusEnabled()) {
                    slider.requestFocus(FocusEvent.Cause.MOUSE_EVENT);
                }
                slider.values.forEach(v -> v.thumbPressed = v.contains(e.getX(),e.getY()));

                //Смотрим, попали ли мы в хоть один из ползунков
                //Если так, то мы начинаем перетаскивать
                var selectThumb = slider.values.stream().filter(v -> v.contains(currentMouseX, currentMouseY)).findAny();
                if (selectThumb.isPresent()) {
                    if (UIManager.getBoolean("Slider.onlyLeftMouseButtonDrag") && !SwingUtilities.isLeftMouseButton(e)) {
                        return;
                    }
                    var thumbRect = selectThumb.get();
                    offset = switch (slider.getOrientation()) {
                        case VERTICAL -> currentMouseY - thumbRect.y;
                        case HORIZONTAL -> currentMouseX - thumbRect.x;
                        default -> throw new IllegalArgumentException(String.valueOf(slider.getOrientation()));
                    };
                    UI.drag(thumbRect);
                    return;
                }

                if (!SwingUtilities.isLeftMouseButton(e)) {
                    return;
                }
                slider.setValueIsAdjusting(true);

                var sbSize = slider.getSize();
                var drag = slider.values.get(0);
                var hip = Math.hypot(currentMouseX - drag.getCenterX(), currentMouseY - drag.getCenterY());
                for (var value : slider.values) {
                    var h2 = Math.hypot(currentMouseX - value.getCenterX(), currentMouseY - value.getCenterY());
                    if(h2 <= hip){
                        drag = value;
                        hip = h2;
                    }
                }
                UI.drag(drag);
                var direction = POSITIVE_SCROLL;
                var drawInverted = UI.drawInverted();
                switch (slider.getOrientation()) {
                    case VERTICAL -> {
                        if ( drag.isEmpty() ) {
                            int scrollbarCenter = sbSize.height / 2;
                            if ( !drawInverted )
                                direction = (currentMouseY < scrollbarCenter) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
                            else
                                direction = (currentMouseY < scrollbarCenter) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
                        } else if ( !drawInverted )
                            direction = (currentMouseY < drag.y) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
                        else
                            direction = (currentMouseY < drag.y) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
                    }
                    case HORIZONTAL -> {
                        if ( drag.isEmpty() ) {
                            int scrollbarCenter = sbSize.width / 2;
                            if ( !drawInverted )
                                direction = (currentMouseX < scrollbarCenter) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
                            else
                                direction = (currentMouseX < scrollbarCenter) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
                        } else if ( !drawInverted )
                            direction = (currentMouseX < drag.x) ? NEGATIVE_SCROLL : POSITIVE_SCROLL;
                        else
                            direction = (currentMouseX < drag.x) ? POSITIVE_SCROLL : NEGATIVE_SCROLL;
                    }
                }                
                if (shouldScroll(direction)) {
                    UI.scrollDueToClickInTrack(direction);
                }
                if (shouldScroll(direction)) {
                    UI.scrollTimer().stop();
                    UI.setDirection(direction);
                    UI.scrollTimer().start();
                }
            }    
            @Override
            public void mouseDragged(MouseEvent e) {
                int thumbMiddle;
                var slider = UI.slider();
                var drawInverted = UI.drawInverted();
                if (!slider.isEnabled()) {
                    return;
                }
                currentMouseX = e.getX();
                currentMouseY = e.getY();

                if (!UI.isDrag())return;
                var drag = UI.drag();
                slider.setValueIsAdjusting(true);
                var trackRect = UI.trackRect();
                var values = UI.slider().values;
                switch (slider.getOrientation()) {
                    case VERTICAL -> {
                        int halfThumbHeight = drag.height / 2;
                        int thumbTop = e.getY() - offset;
                        int trackTop = trackRect.y;
                        int trackBottom = trackRect.y + (trackRect.height - 1);
                        int vMax = UI.yPositionForValue(slider.getMaximum());

                        if (drawInverted) trackBottom = vMax;
                        else trackTop = vMax;
                        thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                        thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                        drag.setLocation(drag.x,thumbTop);

                        thumbMiddle = thumbTop + halfThumbHeight;
                        slider.value(drag, UI.valueForYPosition( thumbMiddle ) );
                    }
                    case HORIZONTAL -> {
                        int halfThumbWidth = drag.width / 2;
                        int thumbLeft = e.getX() - offset;
                        int trackLeft = trackRect.x;
                        int trackRight = trackRect.x + (trackRect.width - 1);
                        int hMax = UI.xPositionForValue(slider.getMaximum());

                        if (drawInverted) trackLeft = hMax;
                        else  trackRight = hMax;
                        thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                        thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                        drag.setLocation(thumbLeft,drag.y);

                        thumbMiddle = thumbLeft + halfThumbWidth;
                        slider.value(drag,UI.valueForXPosition(thumbMiddle));
                    }
                }
                if (slider.getValueIsAdjusting())
                    values.forEach(v -> v.setActive(v == drag));
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                var slider = UI.slider();
                slider.values.forEach(v -> v.setActive(v.contains(e.getX(),e.getY())));
            }
            
            ///Проверяет - можно ли провернуть слайдер на указанное значение
            ///@return true, если надо провернуть
            @Override
            public boolean shouldScroll(int direction) {
                var drag = UI.drag();
                var slider = UI.slider();
                var drawInverted = UI.drawInverted();
                switch (slider.getOrientation()) {
                    case VERTICAL -> {
                        if (drawInverted ? (direction < 0) : (direction > 0)) {
                            if (UI.drag().y  <= currentMouseY) 
                                return false;
                        } else if (drag.y + drag.height >= currentMouseY)
                            return false;
                    }
                    case HORIZONTAL -> {
                        if (drawInverted ? (direction < 0) : (direction > 0)) {
                            if (drag.x + drag.width  >= currentMouseX)
                                return false;
                        } else if (drag.x <= currentMouseX)
                            return false;
                    }
                    default ->
                        throw new IllegalArgumentException(String.valueOf(slider.getOrientation()));
                }
                if (direction > 0 && drag.value >= slider.getMaximum()) {
                    return false;
                } else if (direction < 0 && drag.value <= slider.getMinimum())
                    return false;
                return true;
            }
        }
        //Слушатель события, события
        public static class MainListener implements ChangeListener{
            ///Базовый элемент, который даст нам доступ ко всем нужным полям
            protected final MultiSliderUI UI;
            public MainListener(MultiSliderUI base) {UI = base;}
            
            @Override public void stateChanged(ChangeEvent e) {
                UI.calculateThumbLocation();
                if (!UI.isDrag()) {
                    UI.slider().repaint();
                } else {
                    UI.drag().lastValue = UI.drag().value;
                }
            }
        }
        
        ///Тот ползунок, который мы двигаем
        protected Thumb drag = null;
        @Override public Thumb drag(){return drag;}
        @Override public void drag(Thumb thumb){drag = thumb;}
        @Override public Rectangle trackRect(){return trackRect;}
        @Override public javax.swing.Timer scrollTimer(){return scrollTimer;}
        @Override public boolean drawInverted(){return super.drawInverted();}
        
        public MBasicSliderUI() {super(null);}
        
        @Override public MSlider slider(){return (MSlider) slider;};
        @Override protected TrackListener createTrackListener(JSlider slider) {return new MTrackListener(this);}
        @Override protected ChangeListener createChangeListener(JSlider slider) {return new MainListener(this);}

        @Override public void scrollByBlock(int direction) {scrollByBlock(this,direction);}
        @Override public void scrollByUnit(int direction) {scrollByUnit(this,direction);}

        @Override public void calculateGeometry() { super.calculateGeometry();}
        @Override public void scrollDueToClickInTrack(int dir) {super.scrollDueToClickInTrack(dir);}
        @Override public void setDirection(int dir) {scrollListener.setDirection(dir);}
        @Override public int yPositionForValue(int v){return super.yPositionForValue(v);}
        @Override public int valueForYPosition(int v){return super.valueForYPosition(v);}
        @Override public int xPositionForValue(int v){return super.xPositionForValue(v);}
        @Override public int valueForXPosition(int v){return super.valueForXPosition(v);}
        
        ///Обновляет размеры всех полузнков
        @Override protected void calculateThumbSize() {
            super.calculateThumbSize();
            var size = getThumbSize();
            if(slider().values == null) return;
            slider().values.forEach(v -> v.setSize(size));
        }
        @Override public void calculateThumbLocation() {McalculateThumbLocation(this);}

        @Override
        public void paintThumb(Graphics g) {
            for (var thumb : slider().values)
                paintThumb(thumb, g );
        }
        ///@param g холст, на котором будет нарисован ползунок
        protected void paintThumb(Thumb thumb, Graphics g)  {
            int w = thumb.width;
            int h = thumb.height;

            g.translate(thumb.x, thumb.y);
            Rectangle clip = g.getClipBounds();
            g.clipRect(0, 0, w, h);

            if ( slider.isEnabled() )
                g.setColor(slider.getBackground());
            else
                g.setColor(slider.getBackground().darker());

            var paintThumbArrowShape = (Boolean)slider.getClientProperty("Slider.paintThumbArrowShape");
            if ((!slider.getPaintTicks() && paintThumbArrowShape == null) || paintThumbArrowShape == Boolean.FALSE) {
                // "plain" version
                g.fillRect(0, 0, w, h);

                g.setColor(Color.black);
                g.drawLine(0, h-1, w-1, h-1);
                g.drawLine(w-1, 0, w-1, h-1);

                g.setColor(getHighlightColor());
                g.drawLine(0, 0, 0, h-2);
                g.drawLine(1, 0, w-2, 0);

                g.setColor(getShadowColor());
                g.drawLine(1, h-2, w-2, h-2);
                g.drawLine(w-2, 1, w-2, h-3);
            } else {
                switch (slider.getOrientation()) {
                    case HORIZONTAL -> {
                        int cw = w / 2;
                        g.fillRect(1, 1, w-3, h-1-cw);
                        Polygon p = new Polygon();
                        p.addPoint(1, h-cw);
                        p.addPoint(cw-1, h-1);
                        p.addPoint(w-2, h-1-cw);
                        g.fillPolygon(p);

                        g.setColor(getHighlightColor());
                        g.drawLine(0, 0, w-2, 0);
                        g.drawLine(0, 1, 0, h-1-cw);
                        g.drawLine(0, h-cw, cw-1, h-1);

                        g.setColor(Color.black);
                        g.drawLine(w-1, 0, w-1, h-2-cw);
                        g.drawLine(w-1, h-1-cw, w-1-cw, h-1);

                        g.setColor(getShadowColor());
                        g.drawLine(w-2, 1, w-2, h-2-cw);
                        g.drawLine(w-2, h-1-cw, w-1-cw, h-2);
                    }
                    case VERTICAL -> {
                        int cw = h / 2;
                        if(slider().isLeftToRight()) {
                              g.fillRect(1, 1, w-1-cw, h-3);
                              Polygon p = new Polygon();
                              p.addPoint(w-cw-1, 0);
                              p.addPoint(w-1, cw);
                              p.addPoint(w-1-cw, h-2);
                              g.fillPolygon(p);

                              g.setColor(getHighlightColor());
                              g.drawLine(0, 0, 0, h - 2);                  // left
                              g.drawLine(1, 0, w-1-cw, 0);                 // top
                              g.drawLine(w-cw-1, 0, w-1, cw);              // top slant

                              g.setColor(Color.black);
                              g.drawLine(0, h-1, w-2-cw, h-1);             // bottom
                              g.drawLine(w-1-cw, h-1, w-1, h-1-cw);        // bottom slant

                              g.setColor(getShadowColor());
                              g.drawLine(1, h-2, w-2-cw,  h-2 );         // bottom
                              g.drawLine(w-1-cw, h-2, w-2, h-cw-1 );     // bottom slant
                        } else {
                              g.fillRect(5, 1, w-1-cw, h-3);
                              Polygon p = new Polygon();
                              p.addPoint(cw, 0);
                              p.addPoint(0, cw);
                              p.addPoint(cw, h-2);
                              g.fillPolygon(p);

                              g.setColor(getHighlightColor());
                              g.drawLine(cw-1, 0, w-2, 0);             // top
                              g.drawLine(0, cw, cw, 0);                // top slant

                              g.setColor(Color.black);
                              g.drawLine(0, h-1-cw, cw, h-1 );         // bottom slant
                              g.drawLine(cw, h-1, w-1, h-1);           // bottom

                              g.setColor(getShadowColor());
                              g.drawLine(cw, h-2, w-2,  h-2 );         // bottom
                              g.drawLine(w-1, 1, w-1,  h-2 );          // right
                        }
                    }
                    default ->
                        throw new IllegalArgumentException(String.valueOf(slider.getOrientation()));
                }
            }
            g.setClip(clip);
            g.translate(-thumb.x, -thumb.y);
        }
        
        public static void McalculateThumbLocation(MultiSliderUI UI) {
            var slider = UI.slider();
            if(slider.values == null) return;
            var trackRect = UI.trackRect();
            var mousePosition = slider.getMousePosition();
            for (var i = 0; i < slider.values.size(); i++) {
                var value = slider.values.get(i);
                if ( slider.getSnapToTicks() ) {
                    int sliderValue = value.value;
                    int snappedValue = sliderValue;
                    int tickSpacing = getTickSpacing(slider);

                    if ( tickSpacing != 0 ) {
                        if ( (sliderValue - slider.getMinimum()) % tickSpacing != 0 ) {
                            float temp = (float)(sliderValue - slider.getMinimum()) / (float)tickSpacing;
                            int whichTick = Math.round( temp );

                            if (temp - (int)temp == .5 && sliderValue < value.lastValue)
                              whichTick --;
                            snappedValue = slider.getMinimum() + (whichTick * tickSpacing);
                        }
                        if( snappedValue != sliderValue ) {
                            slider.value( i, snappedValue );
                        }
                    }
                }
                switch (slider.getOrientation()) {
                    case HORIZONTAL -> {
                        int valuePosition = UI.xPositionForValue(value.value);
                        value.setLocation(valuePosition - (value.width / 2), trackRect.y);
                    }
                    case VERTICAL -> {
                        int valuePosition = UI.yPositionForValue(value.value);
                        value.setLocation(trackRect.x, valuePosition - (value.height / 2));
                    }
                    default ->
                        throw new IllegalArgumentException(String.valueOf(slider.getOrientation()));
                }
                if(mousePosition != null) 
                    value.setActive(value.contains(mousePosition.x, mousePosition.y));
            }
        }
        private static int getTickSpacing(MSlider slider) {
            int majorTickSpacing = slider.getMajorTickSpacing();
            int minorTickSpacing = slider.getMinorTickSpacing();

            int result;

            if (minorTickSpacing > 0) {
                result = minorTickSpacing;
            } else if (majorTickSpacing > 0) {
                result = majorTickSpacing;
            } else {
                result = 0;
            }

            return result;
        }
        
        public static void scrollByBlock(MultiSliderUI UI, int direction) {
            var slider = UI.slider();
            synchronized(slider)    {
                var drag = UI.drag();
                var blockIncrement = (slider.getMaximum() - slider.getMinimum()) / 10;
                if (blockIncrement == 0) blockIncrement = 1;
                int tickSpacing = getTickSpacing(slider);
                if (slider.getSnapToTicks()) {
                    if (blockIncrement < tickSpacing)
                        blockIncrement = tickSpacing;
                } else {
                    if (tickSpacing > 0)
                        blockIncrement = tickSpacing;
                }
                int delta = blockIncrement * (direction > 0 ? 1 : -1);
                slider.value(drag,drag.value + delta);
             }
        }
        public static void scrollByUnit(MultiSliderUI UI, int direction) {
            var slider = UI.slider();
            synchronized(slider)    {
                var drag = UI.drag();
                var delta = ((direction > 0) ? POSITIVE_SCROLL : NEGATIVE_SCROLL);
                if (slider.getSnapToTicks())
                    delta *= getTickSpacing(slider);
                slider.value(drag,drag.value + delta);
            }
       }
    }
    //Внешний вид для темы металла
    protected static class MMetalSliderUI extends javax.swing.plaf.metal.MetalSliderUI implements MultiSliderUI{
        ///Тот ползунок, который мы двигаем
        protected Thumb drag = null;
        @Override public Thumb drag(){return drag;}
        @Override public void drag(Thumb thumb){drag = thumb;}
        @Override public Rectangle trackRect(){return trackRect;}
        @Override public javax.swing.Timer scrollTimer(){return scrollTimer;}
        @Override public boolean drawInverted(){return super.drawInverted();}
        
        @Override public MSlider slider(){return (MSlider) slider;};
        @Override protected TrackListener createTrackListener(JSlider slider) {return new MBasicSliderUI.MTrackListener(this);}
        @Override protected ChangeListener createChangeListener(JSlider slider) {return new MBasicSliderUI.MainListener(this);}

        @Override public void scrollByBlock(int direction) {MBasicSliderUI.scrollByBlock(this,direction);}
        @Override public void scrollByUnit(int direction) {MBasicSliderUI.scrollByUnit(this,direction);}
        
        @Override public void calculateGeometry() { super.calculateGeometry();}
        @Override public void scrollDueToClickInTrack(int dir) {super.scrollDueToClickInTrack(dir);}
        @Override public void setDirection(int dir) {scrollListener.setDirection(dir);}
        @Override public int yPositionForValue(int v){return super.yPositionForValue(v);}
        @Override public int valueForYPosition(int v){return super.valueForYPosition(v);}
        @Override public int xPositionForValue(int v){return super.xPositionForValue(v);}
        @Override public int valueForXPosition(int v){return super.valueForXPosition(v);}
        
        ///Обновляет размеры всех полузнков
        @Override protected void calculateThumbSize() {
            super.calculateThumbSize();
            var size = getThumbSize();
            if(slider().values == null) return;
            slider().values.forEach(v -> v.setSize(size));
        }
        @Override public void calculateThumbLocation() {MBasicSliderUI.McalculateThumbLocation(this);}
        @Override
        public void paintThumb(Graphics g) {
            for (var thumb : slider().values)
                paintThumb(thumb, g );
        }
        ///@param g холст, на котором будет нарисован ползунок
        protected void paintThumb(Thumb thumb, Graphics g)  {
            g.translate( thumb.x, thumb.y );
            if ( slider.getOrientation() == JSlider.HORIZONTAL )
                horizThumbIcon.paintIcon( slider, g, 0, 0 );
            else
                vertThumbIcon.paintIcon( slider, g, 0, 0 );
            g.translate( -thumb.x, -thumb.y );
        }
    }
    //Тема Nimbus
    protected static class MSynthSliderUI extends javax.swing.plaf.synth.SynthSliderUI implements MultiSliderUI{
        private class MSTrackListener extends MBasicSliderUI.MTrackListener{
            
            public MSTrackListener(MSynthSliderUI base) {
                super(base);
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                int thumbMiddle;
                var slider = UI.slider();
                var drawInverted = UI.drawInverted();
                if (!slider.isEnabled()) {
                    return;
                }
                currentMouseX = e.getX();
                currentMouseY = e.getY();

                if (!UI.isDrag())return;
                var drag = UI.drag();
                slider.setValueIsAdjusting(true);
                var trackRect = UI.trackRect();
                var values = UI.slider().values;
                switch (slider.getOrientation()) {
                    case VERTICAL -> {
                        int halfThumbHeight = drag.height / 2;
                        int thumbTop = e.getY() - offset;
                        int trackTop = trackRect.y + halfThumbHeight;
                        int trackBottom = trackRect.y + (trackRect.height - 1);
                        int vMax = UI.yPositionForValue(slider.getMaximum());

                        if (drawInverted) trackBottom = vMax;
                        else trackTop = vMax;
                        thumbTop = Math.max(thumbTop, trackTop - halfThumbHeight);
                        thumbTop = Math.min(thumbTop, trackBottom - halfThumbHeight);

                        drag.setLocation(drag.x,thumbTop);

                        thumbMiddle = thumbTop + halfThumbHeight;
                        slider.value(drag, UI.valueForYPosition( thumbMiddle ) );
                    }
                    case HORIZONTAL -> {
                        int halfThumbWidth = drag.width / 2;
                        int thumbLeft = e.getX() - offset;
                        int trackLeft = trackRect.x + halfThumbWidth;
                        int trackRight = trackRect.x + (trackRect.width - 1);
                        int hMax = UI.xPositionForValue(slider.getMaximum());

                        if (drawInverted) trackLeft = hMax;
                        else  trackRight = hMax;
                        thumbLeft = Math.max(thumbLeft, trackLeft - halfThumbWidth);
                        thumbLeft = Math.min(thumbLeft, trackRight - halfThumbWidth);

                        drag.setLocation(thumbLeft,drag.y);

                        thumbMiddle = thumbLeft + halfThumbWidth;
                        slider.value(drag,UI.valueForXPosition(thumbMiddle));
                    }
                }
                if (slider.getValueIsAdjusting())
                    values.forEach(v -> v.setActive(v == drag));
            }
            
        }
        
        public MSynthSliderUI(){super(null);}

        @Override
        public void installUI(JComponent c) {
            super.installUI(c);
            if(slider().values != null)
                thumbRect = slider().values.get(0);
        }
        
        ///Тот ползунок, который мы двигаем
        protected Thumb drag = null;
        @Override public Thumb drag(){return drag;}
        @Override public void drag(Thumb thumb){drag = thumb;}
        @Override public Rectangle trackRect(){return trackRect;}
        @Override public javax.swing.Timer scrollTimer(){return scrollTimer;}
        @Override public boolean drawInverted(){return super.drawInverted();}
        
        @Override public MSlider slider(){return (MSlider) slider;};
        @Override protected TrackListener createTrackListener(JSlider slider) {return new MSTrackListener(this);}
        @Override protected ChangeListener createChangeListener(JSlider slider) {return new MBasicSliderUI.MainListener(this);}

        @Override public void scrollByBlock(int direction) {MBasicSliderUI.scrollByBlock(this,direction);}
        @Override public void scrollByUnit(int direction) {MBasicSliderUI.scrollByUnit(this,direction);}
        
        @Override public void calculateGeometry() { super.calculateGeometry();}
        @Override public void scrollDueToClickInTrack(int dir) {super.scrollDueToClickInTrack(dir);}
        @Override public void setDirection(int dir) {scrollListener.setDirection(dir);}
        @Override public int yPositionForValue(int v){return super.yPositionForValue(v);}
        @Override public int valueForYPosition(int v){return super.valueForYPosition(v);}
        @Override public int xPositionForValue(int v){return super.xPositionForValue(v);}
        @Override public int valueForXPosition(int v){return super.valueForXPosition(v);}
        
        ///Обновляет размеры всех полузнков
        @Override protected void calculateThumbSize() {
            super.calculateThumbSize();
            var size = getThumbSize();
            if(slider().values == null) return;
            slider().values.forEach(v -> v.setSize(size));
        }
        @Override public void calculateThumbLocation() {MBasicSliderUI.McalculateThumbLocation(this);}
        @Override
        protected void paintThumb(javax.swing.plaf.synth.SynthContext context, Graphics g,Rectangle thumbBounds) {
            for (var thumb : slider().values){
                if(thumb.isActive && context.getComponent().isEnabled()){
                    int state = thumb.thumbPressed ? PRESSED : MOUSE_OVER;
                    if (context.getComponent().isFocusOwner()) state |= FOCUSED;
                    var c = new javax.swing.plaf.synth.SynthContext(context.getComponent(), context.getRegion(), context.getStyle(), state);
                    super.paintThumb(c,g,thumb);
                } else {
                    super.paintThumb(context,g,thumb);
                }
            }
        }
    }
    
    ///У нас может быть несколько слайдеров. Поэтому тут список всех слайдеров
    protected List<Thumb> values = new ArrayList<>();
}
