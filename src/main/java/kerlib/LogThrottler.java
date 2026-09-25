///
/// The MIT License
///
/// Copyright 2026 Ilia Pushkin (github.com/Kerravitarr).
///
/// Permission is hereby granted, free of charge, to any person obtaining a copy
/// of this software and associated documentation files (the "Software"), to deal
/// in the Software without restriction, including without limitation the rights
/// to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
/// copies of the Software, and to permit persons to whom the Software is
/// furnished to do so, subject to the following conditions:
///
/// The above copyright notice and this permission notice shall be included in
/// all copies or substantial portions of the Software.
///
/// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
/// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
/// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
/// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
/// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
/// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
/// THE SOFTWARE.
/// 
/// 
/// Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
///

package kerlib;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

///Логгер сообщений с отложенными событиями
///
/// @author Ilia Pushkin (github.com/Kerravitarr)
public class LogThrottler {
    private final ConcurrentHashMap<String, AtomicBoolean> errors = new ConcurrentHashMap<>();
    private final Logger logger;

    public LogThrottler(Class select) {
        this.logger = Logger.getLogger(select.getName());
    }

    // Вызываем, когда случилась ошибка
    public void error(java.util.function.Consumer<Logger> logger) {
        LogThrottler.this.error("",logger);
    }
    public void error(String errorKey, java.util.function.Consumer<Logger> logger) {
        var isLogged = errors.computeIfAbsent(errorKey, k -> new AtomicBoolean(false));
        if (isLogged.compareAndSet(false, true)) {
            logger.accept(this.logger);
        }
    }

    // Вызываем, когда всё снова заработало
    public void recovery(java.util.function.Consumer<Logger> logger) {
        recovery("",logger);
    }
    public void recovery(String errorKey, java.util.function.Consumer<Logger> logger) {
        var isLogged = errors.get(errorKey);
        if (isLogged != null && isLogged.compareAndSet(true, false)) {
            logger.accept(this.logger);
        }
    }
}
