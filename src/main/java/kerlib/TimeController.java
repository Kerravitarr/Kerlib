///
/// Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
/// Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
///

package kerlib;

import java.util.ArrayList;
import java.util.List;

///Класс для отслеживания времени выполнения какого-либо кода.
///
/// @author Ilia Pushkin (github.com/Kerravitarr)
public class TimeController {
    ///Момент запуска - момент создания
    private final long start = System.currentTimeMillis();
    ///Все отслеживаемые шаги
    private final List<Serif> steps = new ArrayList<>();
    ///Когда закончился замер
    private Long stop; // null = измерения ещё идут

    /// @return мс, прошедшие от начала (или от stop, если уже остановлено).
    public long getAll() {
        long end = (stop != null) ? stop : System.currentTimeMillis();
        return end - start;
    }

    ///Добавляет засечку к измерениям.
    public void step() {
        long prefure = steps.isEmpty()
                ? start
                : steps.get(steps.size() - 1).getTime();
        steps.add(new Serif(start, prefure));
    }

    ///Останавливает измерение времени.
    public void stop() {
        this.stop = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < steps.size(); i++) {
            if (i > 0) sb.append('\n');
            sb.append('[').append(i + 1).append("] ").append(steps.get(i));
        }
        sb.append('\n').append("=>").append(msecondsToTime(getAll()));
        return sb.toString();
    }
    ///Переводит милисекунды во время
    public static String msecondsToTime(long ms) {
        if (ms < 1000) return ms + "ms";
        long s = ms / 1000, rms = ms % 1000;
        if (s < 60) return s + "s " + rms + "ms";
        long m = s / 60, rs = s % 60;
        if (m < 60) return m + "m " + rs + "s " + rms + "ms";
        long h = m / 60, rm = m % 60;
        return h + "h " + rm + "m " + rs + "s " + rms + "ms";
    }
    
    
    ///Класс для хранения одной засечки времени.
    public final class Serif {
        ///время начала измерений, мс
        private final long start;
        ///предыдущая засечка, мс
        private final long prefure;
        ///время этой засечки, мс
        private final long time;

        public Serif(long start, long prefure) {
            this.start = start;
            this.prefure = prefure;
            this.time = System.currentTimeMillis();
        }

        /// @return мс, прошедшие от предыдущего шага
        public long getStep() { return time - prefure; }

        /// @return мс, прошедшие от начала засечек
        public long getAll()  { return time - start; }

        /// @return Время засечки — нужен TimeController'у для вычисления prefure следующего шага.
        public long getTime() { return time; }

        @Override
        public String toString() {
            return "+ " + msecondsToTime(getStep()) + " __ " + msecondsToTime(getAll());
        }
    }
}
