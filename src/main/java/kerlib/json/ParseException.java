/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.json;

/**Разные ошибки, возникающие при парсинге файла*/
public class ParseException extends RuntimeException {

    /**Что за ошибка?*/
    private ERROR errorType;
    /**Какой объект мы не ожидали?*/
    private Object unexpectedObject;
    /**Какая позиция в тексте*/
    private long position;

    public ParseException(long position, ERROR errorType, Object unexpectedObject) {
        this.position = position;
        this.errorType = errorType;
        this.unexpectedObject = unexpectedObject;
    }

    public ERROR getErrorType() {
        return errorType;
    }

    public long getPosition() {
        return position;
    }

    public Object getUnexpectedObject() {
        return unexpectedObject;
    }

    @Override
    public String getMessage() {
        StringBuilder sb = new StringBuilder();
        switch (errorType) {
            case UNEXPECTED_CHAR -> sb.append("Неожиданный символ '").append(unexpectedObject).append("' в позиции ").append(position).append(".");
            case UNEXPECTED_TOKEN -> sb.append("Неожиданный токен '").append(unexpectedObject).append("' в позиции ").append(position).append(".");
            case UNEXPECTED_EXCEPTION -> sb.append("Unexpected exception at position ").append(position).append(": ").append(unexpectedObject);
            case UNEXPECTED_VALUE -> sb.append("Неожиданное значение в позиции ").append(position).append(": ").append(unexpectedObject);
            default -> sb.append("Неизвестная ошибка в позиции ").append(position).append(":").append(unexpectedObject).append(".");
        }
        return sb.toString();
    }
    
}
