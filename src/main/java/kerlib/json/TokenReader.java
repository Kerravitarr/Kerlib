/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package kerlib.json;

import java.io.IOException;
import java.io.Reader;

///Чтец токенов
///
/// @author Kerravitarr (github.com/Kerravitarr)
class TokenReader {
    /**Поток, из которого читаем*/
    private final Reader stream;
    /**Текущая позиция чтения*/
    long pos = 0;
    /**Последний символ, который прочитали из потока*/
    char lastChar;
    /**Сдвинули каретку назад, то есть в следующий раз получим предыдущий символ*/
    boolean isBack = false;

    public TokenReader(Reader in) {
        stream = in;
    }

    /**
     * Вычитывает следующий токен из входного потока
     * @return
     * @throws IOException
     * @throws JSON.ParseException
     */
    public Token next() throws IOException, ParseException {
        char ch;
        do {
            if (!stream.ready()) {
                return new Token(JSON_TOKEN.END_DOCUMENT, null);
            }
            ch = read();
        } while (isWhiteSpace(ch));
        return switch (ch) {
        // Не пробел, а что?
            case '{' -> new Token(JSON_TOKEN.BEGIN_OBJECT, "{");
            case '}' -> new Token(JSON_TOKEN.END_OBJECT, "}");
            case '[' -> new Token(JSON_TOKEN.BEGIN_ARRAY, "[");
            case ']' -> new Token(JSON_TOKEN.END_ARRAY, "]");
            case ',' -> new Token(JSON_TOKEN.SEP_COMMA, ",");
            case ':' -> new Token(JSON_TOKEN.SEP_COLON, ":");
            case 'n' -> readNull();
            case 't', 'f' -> readBoolean(ch);
            case '"' -> readString();
        //case '-' -> readNumber(ch);// - входит в def
            default -> readNumber(ch);
        };
    }

    /**
     * Читает число из входного потока
     * @param ch - первое число, может быть числом, а может быть -
     * @return
     * @throws IOException
     * @throws JSON.ParseException
     */
    private Token readNumber(char ch) throws IOException, ParseException {
        var isNegativ = ch == '-';
        if (isNegativ) {
            ch = read();
        }
        if (ch == '0') {
            ch = read();
            if (ch == '.') {
                //Десятичное число 0.ххх
                if (isNegativ) {
                    return new Token(JSON_TOKEN.NUMBER, -readFracAndExp(new StringBuilder("0"), ch));
                } else {
                    return new Token(JSON_TOKEN.NUMBER, readFracAndExp(new StringBuilder("0"), ch));
                }
            } else {
                //Это просто нуль и ни чего более
                back();
                return new Token(JSON_TOKEN.NUMBER, 0);
            }
        } else if (isDigit(ch)) {
            var sb = new StringBuilder();
            do {
                sb.append(ch);
                ch = read();
            } while (isDigit(ch));
            if (ch == '.') {
                // Если это не число, то может точка?
                var val = readFracAndExp(sb, ch);
                return new Token(JSON_TOKEN.NUMBER, isNegativ ? -val : val);
            } else {
                back();
                var long_ = Long.valueOf(sb.toString());
                if (long_ < Integer.MAX_VALUE) {
                    return new Token(JSON_TOKEN.NUMBER, isNegativ ? -long_.intValue() : long_.intValue());
                } else {
                    return new Token(JSON_TOKEN.NUMBER, isNegativ ? -long_ : long_);
                }
            }
        } else if (ch == 'N') {
            //NaN
            if ((ch = read()) == 'a' && (ch = read()) == 'N') {
                return new Token(JSON_TOKEN.NUMBER, Double.NaN);
            } else {
                throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch);
            }
        } else if (ch == 'I') {
            //Infinity
            for (var c : "nfinity".toCharArray()) {
                if ((ch = read()) != c) {
                    throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch);
                }
            }
            return new Token(JSON_TOKEN.NUMBER, isNegativ ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
        } else {
            throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch);
        }
    }

    /** Вычитывает число с плавающей точкой. Но только дробную часть!
     * @param sb буффер, в котором содержится первая часть числа без точки
     * @return
     */
    private double readFracAndExp(StringBuilder sb, char ch) throws IOException, ParseException {
        if (ch == '.') {
            sb.append(ch);
            ch = read();
            if (!isDigit(ch)) {
                throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch);
            }
            do {
                sb.append(ch);
                ch = read();
            } while (isDigit(ch));
            if (isExp(ch)) {
                // А вдруг это экспонента?
                sb.append(ch);
                sb.append(readExp().toString());
            } else {
                back(); // А мы хз что это, не к нам
            }
        } else {
            throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch);
        }
        return Double.parseDouble(sb.toString());
    }

    /**
     * Читает из потока экспоненту
     * @return Число, представляющще собой экспоненту
     * @throws IOException
     * @throws JSON.ParseException
     */
    private Long readExp() throws IOException, ParseException {
        var sb = new StringBuilder();
        var ch = read();
        if (ch == '+' || ch == '-') {
            sb.append(ch);
            ch = read();
            if (isDigit(ch)) {
                do {
                    sb.append(ch);
                    ch = read();
                } while (isDigit(ch));
                back(); //Это не число, дальше мы всё
            } else {
                throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch);
            }
        } else if (isDigit(ch)) {
            do {
                sb.append(ch);
                ch = read();
            } while (isDigit(ch));
            back(); //Это не число, дальше мы всё
        } else {
            throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch);
        }
        return Long.valueOf(sb.toString());
    }

    /** Вычитывает строку из потока
     * @return
     * @throws IOException
     * @throws JSON.ParseException
     */
    private Token readString() throws IOException, ParseException {
        var sb = new StringBuilder();
        while (true) {
            var ch = read();
            switch (ch) {
                case '\\' -> {
                    switch (read()) {
                        case '"' -> sb.append('\"');
                        case '\\' -> sb.append('\\');
                        case '/' -> sb.append('/');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'u' -> {
                            var unix = new StringBuilder();
                            for (var i = 0; i < 4; i++) {
                                ch = read();
                                if (isHex(ch)) {
                                    unix.append(ch);
                                } else {
                                    throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch);
                                }
                            }
                            sb.append(Character.toChars(Integer.parseInt(unix.toString(), 16)));
                        }
                        default -> throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch);
                    }
                }
                case '"' -> {
                    return new Token(JSON_TOKEN.STRING, sb.toString());
                }
                case '\r', '\n' -> throw new ParseException(pos, ERROR.UNEXPECTED_CHAR, ch); //Не может быть тут таких символов
                default -> sb.append(ch);
            }
        }
    }

    /**
     * Вычитывает значение true/false
     * @param ch - первый символ слова true или false
     * @return токен, который вычитает - Boolean
     * @throws IOException
     * @throws JSON.ParseException
     */
    private Token readBoolean(char ch) throws IOException, ParseException {
        if (ch == 't') {
            var buf = new char[3];
            pos += stream.read(buf);
            if (!(buf[0] == 'r' && buf[1] == 'u' && buf[2] == 'e')) {
                throw new ParseException(pos, ERROR.UNEXPECTED_VALUE, "t" + buf[0] + buf[1] + buf[2]);
            } else {
                return new Token(JSON_TOKEN.BOOLEAN, true);
            }
        } else {
            var buf = new char[4];
            pos += stream.read(buf);
            if (!(buf[0] == 'a' && buf[1] == 'l' && buf[2] == 's' && buf[3] == 'e')) {
                throw new ParseException(pos, ERROR.UNEXPECTED_VALUE, "f" + buf[0] + buf[1] + buf[2] + buf[3]);
            } else {
                return new Token(JSON_TOKEN.BOOLEAN, false);
            }
        }
    }

    /**
     * Вычитывает значение null
     * @return токен, который вычитывает - токен null
     * @throws IOException
     * @throws JSON.ParseException
     */
    private Token readNull() throws IOException, ParseException {
        var buf = new char[3];
        pos += stream.read(buf);
        if (!(buf[0] == 'u' && buf[1] == 'l' && buf[2] == 'l')) {
            throw new ParseException(pos, ERROR.UNEXPECTED_VALUE, "n" + buf[0] + buf[1] + buf[2]);
        } else {
            return new Token(JSON_TOKEN.NULL, "null");
        }
    }

    /**Првоеряет, что символ является числом*/
    private boolean isDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    /** Проверяет, что символ относится к экспоненциальной записи числа */
    private boolean isExp(char ch) {
        return ch == 'e' || ch == 'E';
    }

    /**Проверяет, что символ относится к хексам*/
    private boolean isHex(char ch) {
        return (ch >= '0' && ch <= '9') || ('a' <= ch && ch <= 'f') || ('A' <= ch && ch <= 'F');
    }

    /**Показывает, является-ли введённый символ пробелом (таблом, ентером и т.д.)*/
    private boolean isWhiteSpace(char ch) {
        return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
    }

    /**Показывает, можно-ли ещё вычитать из буфера что либо*/
    public boolean hasNext() {
        try {
            return stream.ready();
        } catch (IOException e) {
            return false;
        }
    }

    /**Читает символ из потока. Запоминает прочитанный символ во временный буфер*/
    private char read() throws IOException {
        if (!isBack) {
            lastChar = (char) stream.read();
        } else {
            isBack = false;
        }
        pos++;
        return lastChar;
    }

    /**Сдвинуть курсор чтения на одну позицию назад*/
    private void back() {
        isBack = true;
        pos--;
    }
    
}
