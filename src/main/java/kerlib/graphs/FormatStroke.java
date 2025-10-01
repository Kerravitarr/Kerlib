/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

import java.awt.BasicStroke;

/**
 *
 * @author Kerravitarr (github.com/Kerravitarr)
 */
public class FormatStroke extends BasicStroke {

    /**
     * Создает новый {@code FormatStroke} с указанными атрибутами.
     * @param width ширина этого {@code FormatStroke}.
     * 		Ширина должна быть больше или равна 0,0f.
     * 		Если для ширины установлено значение 0,0f,
     * 		обводка отображается как самая тонкая из возможных.
     * @param dash массив, представляющий штриховой узор
     * @param dash_phase смещение для начала штриховки
     */
    public FormatStroke(float width, float[] dash, float dash_phase) {
        super(width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 1, dash, dash_phase);
    }

    public FormatStroke(float width, float[] dash) {
        this(width, dash, 0);
    }

    public FormatStroke(float width, Bash dash) {
        this(width, dash.dash, dash.dash_phase);
    }

    public FormatStroke(Bash dash) {
        this(0, dash.dash, dash.dash_phase);
    }

    public FormatStroke(float width) {
        this(width, new float[]{Float.MAX_VALUE}, 0);
    }

}
