/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package kerlib.json;

///Возможные ошибки при парсинге файла
/// @author Kerravitarr (github.com/Kerravitarr)
public enum ERROR {
    ///Символ, который не ожидали
    UNEXPECTED_CHAR, 
    ///Токен, который не ожидали
    UNEXPECTED_TOKEN, 
    ///Неожиданное исключение в позиции
    UNEXPECTED_EXCEPTION, 
    //Неизвестный тип значения
    UNEXPECTED_VALUE, 
    //Что-то совсем уж странное
    UNKNOW
}
