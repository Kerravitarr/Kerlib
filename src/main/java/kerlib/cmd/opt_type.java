/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package kerlib.cmd;

/**Тип опции*/
public enum opt_type {
    /**Заглушка, опция которой несуществует*/
    _void, /**Число*/ _int, //Число
    /**Логика, есть опция/нет*/
    _bool, //Логика, есть опция/нет
    /**Строка*/
    _string, //Строка
    /**Символ*/
    _char, //Символ
    /**Вектор чисел из строки, разделённе особым символом*/
    _v_int, //Вектор чисел из строки, разделённе особым символом
    /**Вектор строк, разделённых  особым символом*/
    _v_string, //Вектор строк, разделённых  особым символом
    /**Вектор символов из строки, разделённой  особым символом*/
    _v_char
    
}
