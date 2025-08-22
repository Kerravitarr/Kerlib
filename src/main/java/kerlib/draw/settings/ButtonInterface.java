/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw.settings;

/**Глобальный интерфейс. для настроек. Он служит для обновления состояния кнопок. На вход попадает голая кнопка и ключ
 * Предполагается, что некоторые кнопки будут иметь особенный вид и подписи... Ну а если нет - то можно просто текст на кнопку
 * наложить и успокоиться
 * @author zeus
 */
public interface ButtonInterface {    
    enum Key{
        ///Кнопка сброса значения по умолчанию
        RESET,
        ;
    }
    
    /**Создание кнопки по ключу
     * @param key ключ кнопки
     * @param button объект кнопки
     */
    public void make(Key key,javax.swing.AbstractButton button);
}
