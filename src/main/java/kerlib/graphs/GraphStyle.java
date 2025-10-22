/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package kerlib.graphs;

import java.awt.Graphics2D;

/**
 *Стиль графика
 * @author Kerravitarr (github.com/Kerravitarr)
 */
public interface GraphStyle {
    /**Установить стиль
     * @param g
     */
    public void set(Graphics2D g);
    /**Снять стиль
     * @param g
     */
    public void unset(Graphics2D g);
}
