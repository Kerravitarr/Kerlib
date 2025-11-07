/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.json;

///Ошбики, которые возникают при разборе объекта JSON
///
/// @author Kerravitarr (github.com/Kerravitarr)
public class ParseException extends RuntimeException {

    ///Тип ошибки
    private final ERROR errorType;
    ///Вспомогательные данные по ошибке
    private final Object unexpectedObject;
    ///Позиция в разбираемом объекте
    private final long position;

    public ParseException(long position, ERROR errorType, Object unexpectedObject) {
        this.position = position;
        this.errorType = errorType;
        this.unexpectedObject = unexpectedObject;
    }
    ///@return тип ошибки
    public ERROR getErrorType() {
        return errorType;
    }
    ///@return позиция ошибки
    public long getPosition() {
        return position;
    }
    ///@return вспомогательный объект этой ошибки
    public Object getUnexpectedObject() {
        return unexpectedObject;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        switch (errorType) {
            case UNEXPECTED_CHAR -> sb.append("Неожиданный символ '").append(unexpectedObject).append("' в позиции ").append(position).append(".");
            case UNEXPECTED_TOKEN -> sb.append("Неожиданный токен '").append(unexpectedObject).append("' в позиции ").append(position).append(".");
            case UNEXPECTED_EXCEPTION -> sb.append("Неожиданное исключение в позиции ").append(position).append(": ").append(unexpectedObject);
            case UNEXPECTED_VALUE -> sb.append("Неожиданное значение в позиции ").append(position).append(": ").append(unexpectedObject);
            default -> sb.append("Неизвестная ошибка в позиции ").append(position).append(":").append(unexpectedObject).append(".");
        }
        return sb.toString();
    }
    
}
