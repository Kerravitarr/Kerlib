/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.json;

/**
 *Перечисление разных состояний парсинга файла
 * @author zeus
 */
enum JSON_TOKEN{
    BEGIN_OBJECT("{"), END_OBJECT("}"), BEGIN_ARRAY("["), END_ARRAY("]"), NULL("null"), NUMBER("number"),
    STRING("str"), BOOLEAN("true/false"), SEP_COLON(":"), SEP_COMMA(","), END_DOCUMENT("");
    /**Описание символа*/
    @SuppressWarnings("unused")
    private String help;
    /**Номер перечисления, уникальный бит*/
    int value;
    JSON_TOKEN(String help) {this.help=help;value = 1 << this.ordinal();}
}
