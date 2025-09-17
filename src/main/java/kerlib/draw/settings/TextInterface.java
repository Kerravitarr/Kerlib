/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.draw.settings;

/**Глобальный интерфейс. для настроек. Он служит для того, чтобы настройки знали как получить текстовое описание своего поля
 * @author zeus
 */
public interface TextInterface {    
    enum Key{
        ///Основаня подпись. Лаконично, что это за настройки
        LABEL,
        ///Подпись, которая появляется при наведении на настройки
        TOOLTIPTEXT,
        ///Подпись над кнопкой, которая отвечает за сброс значения. Появляется при наведении на эту кнопку
        RESET_B_TOOLTIPTEXT,
        ///Подпись над кнопкой, которая есть только у PointPanel. Это кнопка выбора точки на экране
        SELECT_B_TOOLTIPTEXT,
        ///Только для PointPanel - подпись для слайдера Х
        LABEL_X,
        ///Только для PointPanel - подпись для слайдера У
        LABEL_Y,
        ///Только для PointPanel. Когда появляется выбор точки на экране, вот этот текст будет показан прежде чем появится какое либо значение
        SELECT_LABEL_EMPTY,
        ///Только для PointPanel. Когда появляется выбор точки на экране, отсюда панель получит текст в формате MessageFormat, затем отформатирует его с передачей х и у
        SELECT_LABEL_FORMAT,
        ;
    }
    
    /**Получение значения текста по ключу
     * @param key ключ. Какой ключ передан, то и надо вернуть
     * @return 
     */
    public String text(Key key);
}
