package kerlib.draw;

import java.awt.Color;

///Класс, определяющий результирующий цвет на основе цветового круга и прогресса на этом кругу
/// Позволяет создавать круговые градиенты
/// Задать цвета от начала и до конца
/// А потом сдвигаться по градиенту как прогресс от 0 до 1
/// 
/// @author Kerravitarr (github.com/Kerravitarr)
public class ColorGradient {
    ///Список всех уже посчитанных цветов
    private final Color[] colors;
    ///На сколько прогресс может отставать, быть меньше 0%
    private final double negProgress;
    ///На сколько прогресс может опережать, выше 100%
    private final double posProgress;
    ///Ширина прогресса, от скольки до скольки
    private final double progressWidth;

    ///Создаёт круговой градиент из 256 цветов окружающий выбранный цвет как радуга: Красный-Оранжевый-Жёлтый-Зелёный-Голубой-Синий-Фиолетовый-Красный
    /// @param color от какого и до какого цвета будет круг
    public ColorGradient(Color color) {
        this(color, true);
    }
    ///Создаёт круговой градиент из 256 цветов
    /// @param color от какого и до какого цвета будет круг. По факту круг вокруг цвета
    /// @param isRainbow переход Красный-Оранжевый-Жёлтый-Зелёный-Голубой-Синий-Фиолетовый-Красный или обратный?
    public ColorGradient(Color color, boolean isRainbow) {
        this(color, color, isRainbow);
    }

    ///Создаёт круговой градиент между выбранными цветами
    /// @param from от какого цвета
    /// @param to к какому цвету
    /// @param isRainbow переход Красный-Оранжевый-Жёлтый-Зелёный-Голубой-Синий-Фиолетовый-Красный или обратный?
    public ColorGradient(Color from, Color to, boolean isRainbow) {
        this(from, to, isRainbow, 256);
    }
    ///Создаёт круговой градиент между выбранными цветами
    /// @param from от какого цвета
    /// @param to к какому цвету
    /// @param isRainbow переход Красный-Оранжевый-Жёлтый-Зелёный-Голубой-Синий-Фиолетовый-Красный или обратный?
    /// @param accuracy точность, сколько переходных цветов будет
    public ColorGradient(Color from,Color to,boolean isRainbow, int accuracy) {
        this(from, 0, to, 0, isRainbow, accuracy);
    }
    ///Создаёт круговой градиент
    /// @param from от какого цвета
    /// @param fromOffset на сколько прогресс может отступать от 0 в меньшую сторону.
    ///         Может быть полезно, если начальный цвет не является минимально возможным.
    ///         И наоборот, если задать отрицательное значение, то начальный цвет не будет минимальным
    /// @param to к какому цвету
    /// @param toOffset на сколько прогресс может заходить за 1 в большую сторону.
    ///         Может быть полезно, если конечный цвет не является максимально возможным.
    ///         И наоборот, если задать отрицательное значение, то конечный цвет не будет максимальным
    /// @param isRainbow переход Красный-Оранжевый-Жёлтый-Зелёный-Голубой-Синий-Фиолетовый-Красный или обратный?
    /// @param accuracy точность, сколько переходных цветов будет
    public ColorGradient(Color from,float fromOffset, Color to,float toOffset, boolean isRainbow, int accuracy) {
        colors = new Color[accuracy];
        negProgress = 0 - fromOffset;
        posProgress = 1 + toOffset;
        progressWidth = posProgress - negProgress;
        if(negProgress > posProgress) throw new IllegalArgumentException("Ширина окна для цвета отрицательная при смещении от начала " + fromOffset + " и до конца " + toOffset);
        else if(progressWidth == 0) throw new IllegalArgumentException("Ширина окна  для цвета нулевая  при смещении от начала " + fromOffset + " и до конца " + toOffset);
        else if(accuracy <= 0) throw new IllegalArgumentException("Количество цвето должно быть положительным значением!");
        
        var hsbfrom = new float[4];
        var hsbto = new float[4];
        Color.RGBtoHSB(from.getRed(), from.getGreen(), from.getBlue(), hsbfrom);
        hsbfrom[3] = from.getAlpha() / 255f;
        Color.RGBtoHSB(to.getRed(), to.getGreen(), to.getBlue(), hsbto);
        hsbto[3] = to.getAlpha() / 255f;
        
        hsbfrom[0] -= fromOffset;
        hsbto[0] += toOffset;
        
        var params = new float[4];
        if (isRainbow){
            params[0] = hsbto[0] - hsbfrom[0];
            if(params[0] < 0) params[0] += 1f;
        } else {
            params[0] = -(hsbto[0] - hsbfrom[0]);
            if(params[0] < 0) params[0] += 1f;
            params[0] = -params[0];
        }
        if(params[0] == 0) params[0] = isRainbow ? 1f : -1f;
        for (var i = 1; i < params.length; i++)
            params[i] = (hsbto[i] - hsbfrom[i]);
        for (int i = 0; i < params.length; i++)
            params[i] /= ((float)accuracy);
        for (int i = 0; i < colors.length; i++)
            colors[i] = getHSBColor(hsbfrom[0] + params[0] * i, hsbfrom[1] + params[1] * i, hsbfrom[2] + params[2] * i, hsbfrom[3] + params[3] * i);
    }

    ///Возвращает цвет на основе прогресса
    /// @param progress прогресс по шклае [0,1]. Может быть больше 1, тогда пойдёт на второй круг.
    /// Если заданно значение fromOffset и toOffset заданы, то progress должен быть в интервале [-fromOffset;1+toOffset], иначе цвет пойдёт на второй круг
    /// @return цвет, согласно заданному прогрессу
    public Color get(double progress) {
        if(progress < negProgress || progress > posProgress)
            progress = ((progress - negProgress) % progressWidth) + negProgress;
        var p = colors.length * (progress - negProgress) / progressWidth;
        return colors[kerlib.tools.betwin(0, (int) p, colors.length-1)];
    }

    ///Преобразует цвет HSB в нормальный цвет
    /// @param h 0..1 цвет
    /// @param s 0..1 насыщенность
    /// @param b 0..1 яркость
    /// @param a 0..1 прозрачность
    /// @return цвет
    private Color getHSBColor(float h, float s, float b, float a) {
        while (h > 1) h -= 1f;
        while (h < 0) h += 1f;
        s = kerlib.tools.betwin(0f, s, 1f);
        b = kerlib.tools.betwin(0f, b, 1f);
        a = kerlib.tools.betwin(0f, a, 1f);
        return kerlib.draw.tools.getHSBColor(h, s, b, a);
    }

}
