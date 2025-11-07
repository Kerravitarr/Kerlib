/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.json;

///Один прочитанный токен из потока
///
/// @author Kerravitarr (github.com/Kerravitarr)
class Token {
    public Token(JSON_TOKEN type, Object value) {
        this.type = type;
        this.value = value;
    }
    ///Что за токен
    public final JSON_TOKEN type;
    ///Объект, скрывающийся за этим токеном
    public final Object value;

    @Override
    public String toString() {
        return type + " " + value;
    }
    
}
