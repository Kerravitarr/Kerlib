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

package kerlib.graphs.styles;

import java.awt.Graphics2D;
import java.util.function.Consumer;
import kerlib.graphs.GraphStyle;

///Стилистика линии графика. Определяет как будет выглядеть линия, всякие там штриховки и прочее
///
///
/// @author Ilia Pushkin (github.com/Kerravitarr)
public class Stroke extends GraphStyle {
    ///Стиль оформления концов линии
    public enum StrokeCap{
        ///Завершает незамкнутые подконтуры и сегменты штрихов без добавления декора.
        CAP_BUTT(java.awt.BasicStroke.CAP_BUTT),
        ///Завершает незамкнутые подконтуры и сегменты штрихов круглым украшением, радиус которого равен половине ширины пера.
        CAP_ROUND(java.awt.BasicStroke.CAP_ROUND),
        ///Завершает незамкнутые подконтуры и сегменты штрихов квадратным выступом, выходящим за конец сегмента на расстояние, равное половине ширины линии.
        CAP_SQUARE(java.awt.BasicStroke.CAP_SQUARE),;
        
        ///Константа этого значения, в системе базового стиля
        public final int BS_CONST;
        private StrokeCap(int BSCONST) {
            this.BS_CONST = BSCONST;
        }
    }
    ///Стиль оформления соединений линий
    public enum StrokeJoin{
        ///Объединяет сегменты пути путем удлинения их внешних краев до их встречи.
        JOIN_MITER(java.awt.BasicStroke.JOIN_MITER),
        ///Соединяет сегменты пути путем скругления угла с радиусом, равным половине ширины линии.
        JOIN_ROUND(java.awt.BasicStroke.JOIN_ROUND),
        ///Объединяет сегменты пути путем соединения внешних углов их широких контуров прямым сегментом.
        JOIN_BEVEL(java.awt.BasicStroke.JOIN_BEVEL),;
        
        ///Константа этого значения, в системе базового стиля
        public final int BS_CONST;
        private StrokeJoin(int BSCONST) {
            this.BS_CONST = BSCONST;
        }
    }
    ///Задаёт толщину линию
    ///@param width толщина линии {@code Stroke}
    ///@throws IllegalArgumentException, если {@code width} отрицательна
    public Stroke(float width) {this(new java.awt.BasicStroke(width));} 
    ///Задаёт штриховку линию
    /// @param dash массив, представляющий шаблон штриховки
    /// @throws IllegalArgumentException, если длина {@code dash} равно нулю
    /// @throws IllegalArgumentException, если длина всех элементов {@code dash} равна нулю.
    public Stroke(float... dash) {this(dash,0);} 
    ///Задаёт штриховку линию
    /// @param dash массив, представляющий шаблон штриховки
    /// @param dash_phase смещение для начала шаблона штриховки
    /// @throws IllegalArgumentException, если {@code dash_phase} отрицательное и {@code dash} не равен {@code null}
    /// @throws IllegalArgumentException, если длина {@code dash} равно нулю
    /// @throws IllegalArgumentException, если длина всех элементов {@code dash} равна нулю.
    public Stroke(float[] dash, float dash_phase) {this(1f,StrokeCap.CAP_SQUARE,StrokeJoin.JOIN_MITER, 10f, dash, dash_phase);} 
    ///Задаёт толщину линию и способы соедения линий
    ///@param width толщина линии {@code Stroke}
    ///@param cap оформление концов {@code Stroke}
    ///@param join оформление, применяемое к стыкам сегментов контура {@code Stroke}
    ///@throws IllegalArgumentException, если {@code width} отрицательна
    public Stroke(float width, StrokeCap cap, StrokeJoin join) {this(new java.awt.BasicStroke(width,cap.BS_CONST,join.BS_CONST));} 
    ///Задаёт толщину линию и способы соедения линий
    /// @param width толщина линии {@code Stroke}
    /// @param cap оформление концов {@code Stroke}
    /// @param join оформление, применяемое к стыкам сегментов контура {@code Stroke}
    /// @param miterlimit ограничение для обрезки углового соединения
    /// @throws IllegalArgumentException, если {@code width} отрицательна
    /// @throws IllegalArgumentException, если {@code miterlimit} меньше 1, а {@code join} не является JOIN_MITER
    public Stroke(float width, StrokeCap cap, StrokeJoin join, float miterlimit) {this(new java.awt.BasicStroke(width,cap.BS_CONST,join.BS_CONST,miterlimit));} 
    ///Задаёт толщину линию и способы соедения линий
    /// @param width толщина линии {@code Stroke}
    /// @param cap оформление концов {@code Stroke}
    /// @param join оформление, применяемое к стыкам сегментов контура {@code Stroke}
    /// @param miterlimit ограничение для обрезки углового соединения
    /// @param dash массив, представляющий шаблон штриховки
    /// @param dash_phase смещение для начала шаблона штриховки
    /// @throws IllegalArgumentException, если {@code width} отрицательна
    /// @throws IllegalArgumentException, если {@code miterlimit} меньше 1, а {@code join} не является JOIN_MITER
    /// @throws IllegalArgumentException, если {@code dash_phase} отрицательное и {@code dash} не равен {@code null}
    /// @throws IllegalArgumentException, если длина {@code dash} равно нулю
    /// @throws IllegalArgumentException, если длина всех элементов {@code dash} равна нулю.
    public Stroke(float width, StrokeCap cap, StrokeJoin join, float miterlimit,float[] dash, float dash_phase) {this(new java.awt.BasicStroke(width,cap.BS_CONST,join.BS_CONST,miterlimit,dash,dash_phase));} 
    ///Создаёт стиль линии на основании заданной стилистики
    /// @param stroke стилистика линии
    public Stroke(java.awt.Stroke stroke) {
        this.stroke = stroke;
    }
    
    @Override
    protected Consumer<Graphics2D> add(Graphics2D g) {
        var ob = g.getStroke();
        g.setStroke(stroke);
        return n -> n.setStroke(ob);
    }

    ///Объект рисования 
    private final java.awt.Stroke stroke;
}
