///
/// The MIT License
///
/// Copyright 2025 Ilia Pushkin (github.com/Kerravitarr).
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

package kerlib.jpromise;

import java.util.concurrent.*;
import java.util.function.*;


///
/// Реализация Promise для Java, аналогичная JavaScript Promise
/// @author Ilia Pushkin (github.com/Kerravitarr)
/// @param <T> тип отложенного обращения
public class Promise<T> {
    /// Исключение для завершения выполнения Promise
    public static class PromiseEnd extends RuntimeException{
        private PromiseEnd(){};
    }
    
    /// Исполнитель для виртуальных потоков
    private static final ExecutorService virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
    /**Логгер для записи сообщений о работе класса*/
    private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(PromiseEnd.class.getName());
    ///Глобальное исключение. Зачем создавать каждый раз новое, если нужно всего одно?
    private static final PromiseEnd THROW = new PromiseEnd();
    
    /// Внутренний CompletableFuture для асинхронного выполнения
    private CompletableFuture<T> future;
    /// Значение при успешном выполнении
    private T resolvedValue;
    /// Ошибка при неудачном выполнении
    private Throwable rejectedError;
    /// Флаг завершения Promise
    private volatile boolean isSettled = false;
    ///Флаг, что кто-то нашу ошбику заберёт себе
    private volatile boolean isSilentError = false;
    
    /// Конструктор с только resolve функцией
    /// ВНИМАНИЕ!!! Вызов функции приводит к возникновению исключения PromiseEnd!!!
    /// @param resolve функция, вызываемая когда действие завершилось
    public Promise(Consumer<Consumer<T>> resolve) {
        this((a,_) -> {resolve.accept(a);});
    }
    
    /// Конструктор с resolve функцией.
    ///Позволяет создвать объекты типа Promise(resolve -> {
    ///     ...
    ///     return resolve.apply(ret);
    /// })
    /// @param resolve фнукция, показывающая, что действие завершилось
    public Promise(Function<Function<T,PromiseEnd>,PromiseEnd> resolve) {
        this((BiFunction<Function<T,PromiseEnd>,Function<Throwable,PromiseEnd>,PromiseEnd>)(a,b) ->{
            return resolve.apply(result -> a.apply(result));
        });
    }
    
    /**
     * Создает Promise с функциями resolve и reject.
     * Конструктор создаётся в виде:
     * new Promise((resolve, reject) -> {
     *     ...
     *     resolve.apply(ret);
     *     ...
     *     reject.apply(error);
     * });
     * И обе функции, resolve и reject при вызове создают исключение PromiseEnd!!!
     * @param executor функция-исполнитель с resolve и reject callback'ами
     */
    public Promise(BiConsumer<Consumer<T>,Consumer<Throwable>> executor) {
        this((BiFunction<Function<T,PromiseEnd>,Function<Throwable,PromiseEnd>,PromiseEnd>)(a,b) ->{
            executor.accept(result -> {throw a.apply(result);}, error -> {throw b.apply(error);});
            return THROW;
        });
    }
    /**
     * Основной конструктор с полным контролем над resolve/reject.
     * Конструктор позволяет создать задание в виде:
     * new Promise((resolve, reject) -> {
     *     ...
     *     return resolve.apply(ret);
     *     ...
     *     return reject.apply(error);
     * });
     * @param executor функция-исполнитель с resolve и reject функциями
     */
    public Promise(BiFunction<Function<T,PromiseEnd>,Function<Throwable,PromiseEnd>,PromiseEnd> executor) {        
        this.future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            // Функция для успешного завершения Promise
            Function<T,PromiseEnd> resolve = result -> {
                synchronized (Promise.this) {
                    if (isSettled) return THROW;
                    this.resolvedValue = result;
                    future.complete(result);
                    isSettled = true;
                    Promise.this.notifyAll();
                }
                return THROW;
            };
            // Функция для завершения Promise с ошибкой
            Function<Throwable,PromiseEnd> reject = error -> {
                synchronized (Promise.this) {
                    if (isSettled) return THROW;
                    
                    this.rejectedError = error;
                    future.completeExceptionally(error);
                    
                    isSettled = true;
                    Promise.this.notifyAll();
                }
                if(!isSilentError){
                    logger.log(java.util.logging.Level.SEVERE, "Произошла ошибка во время выполнения", error);
                }
                return THROW;
            };
            try {
                // Выполняем executor и ожидаем PromiseEnd исключение
                executor.apply(resolve, reject);
            } catch (PromiseEnd e) {
                // Нормальное завершение через PromiseEnd
                synchronized (Promise.this) {
                    if (isSettled) return;
                    this.rejectedError = THROW;
                    future.completeExceptionally(THROW);
                    isSettled = true;
                    Promise.this.notifyAll();
                }
            } catch (Exception e) {
                // Обработка неожиданных исключений
                synchronized (Promise.this) {
                    this.rejectedError = e;
                    future.completeExceptionally(e);
                    isSettled = true;
                    Promise.this.notifyAll();
                }
                if(!isSilentError){
                    logger.log(java.util.logging.Level.SEVERE, "Произошла ошибка во время ожидания результата", e);
                }
            }
        }
        ,virtualExecutor);
    }
    
    // Приватный конструктор для внутреннего использования (then, catch, finally)
    private Promise(Object none, Consumer<BiConsumer<T, Throwable>> executor) {
        this.future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                // Выполняем executor с callback для результата или ошибки
                executor.accept((result, error) -> {
                    synchronized (Promise.this) {
                        if (isSettled) return;
                        
                        if (error != null) {
                            this.rejectedError = error;
                            future.completeExceptionally(error);
                        } else {
                            this.resolvedValue = result;
                            future.complete(result);
                        }
                        isSettled = true;
                        Promise.this.notifyAll();
                    }
                });
            } catch (Exception e) {
                synchronized (Promise.this) {
                    this.rejectedError = e;
                    future.completeExceptionally(e);
                    isSettled = true;
                    Promise.this.notifyAll();
                }
            }
        }, virtualExecutor);
    }
    
    /**
     * Добавляет обработчик успешного выполнения Promise.
     * @param onfulfilled функция обработки успешного результата
     * @return Promise без явного изменения резульатата
     */
    public Promise<T> then(Consumer<T> onfulfilled) {
        return then(onfulfilled, null);
    }
    /**
     * Добавляет обработчики успешного выполнения и ошибки Promise.
     * @param onfulfilled функция обработки успешного результата
     * @param onrejected функция обработки ошибки
     * @return Promise без явного изменения резульатата
     */
    public Promise<T> then(Consumer<T> onfulfilled, Consumer<Throwable> onrejected) {
        future.whenComplete((result, error) -> {
            if (error != null) {
                if(onrejected != null)onrejected.accept(error);
                else logger.log(java.util.logging.Level.SEVERE, null, error);
            } else {
                onfulfilled.accept(result);
            }
        });
        return this;
    }
    /**
     * Добавляет обработчик успешного выполнения Promise.
     * @param onfulfilled функция обработки успешного результата
     * @return Promise без явного изменения резульатата
     */
    public Promise<T> thenc(Consumer<T> onfulfilled) {return then(onfulfilled);}
    /**
     * Добавляет обработчики успешного выполнения и ошибки Promise.
     * @param onfulfilled функция обработки успешного результата
     * @param onrejected функция обработки ошибки
     * @return Promise без явного изменения резульатата
     */
    public Promise<T> thenc(Consumer<T> onfulfilled, Consumer<Throwable> onrejected) {return then(onfulfilled,onrejected);}
    /**
     * Добавляет обработчик успешного выполнения Promise.
     * @param <R> тип результата нового Promise
     * @param onfulfilled функция обработки успешного результата
     * @return новый Promise с результатом обработки
     */
    public <R> Promise<R> then(Function<T, R> onfulfilled) {
        return then(onfulfilled,null);
    }
    /**
     * Добавляет обработчики успешного выполнения и ошибки Promise.
     * @param <R> тип результата нового Promise
     * @param onfulfilled функция обработки успешного результата
     * @param onrejected функция обработки ошибки
     * @return новый Promise с результатом обработки
     */
    public <R> Promise<R> then(Function<T, R> onfulfilled, Function<Throwable, R> onrejected) {
        return new Promise<>(null,resolve -> {
            this.future.whenComplete((result, error) -> {
                if (error != null) {
                    // Обработка ошибки
                    if(onrejected != null){
                        try {
                            var nextStep = onrejected.apply(error);
                            resolve.accept(nextStep, null);
                        } catch (Exception e) {
                            resolve.accept(null, e);
                        }
                    } else {
                        resolve.accept(null, error);
                    }
                } else {
                    // Обработка успешного результата
                    try {
                        R newResult = onfulfilled.apply(result);
                        resolve.accept(newResult, null);
                    } catch (Exception e) {
                        // Если при обработке успеха возникла ошибка
                        if(onrejected != null){
                            try {
                                var nextStep = onrejected.apply(e);
                                resolve.accept(nextStep, null);
                            } catch (Exception e2) {
                                resolve.accept(null, e2);
                            }
                        } else {
                            resolve.accept(null, e);
                        }
                    }
                }
            });
        });
    }
    
    /**
     * Добавляет обработчик успешного выполнения Promise.
     * @param <R> тип результата нового Promise
     * @param onfulfilled функция обработки успешного результата
     * @return новый Promise с результатом обработки
     */
    public <R> Promise<R> thenf(Function<T, R> onfulfilled) {return then(onfulfilled);}
    /**
     * Добавляет обработчики успешного выполнения и ошибки Promise.
     * @param <R> тип результата нового Promise
     * @param onfulfilled функция обработки успешного результата
     * @param onrejected функция обработки ошибки
     * @return новый Promise с результатом обработки
     */
    public <R> Promise<R> thenf(Function<T, R> onfulfilled, Function<Throwable, R> onrejected) {return then(onfulfilled,onrejected);}
    
    /**
     * Добавляет обработчик ошибок Promise.
     * @param onrejected функция обработки ошибки
     * @return новый Promise с результатом обработки ошибки
     */
    public Promise<T> catchError(Function<Throwable, T> onrejected) {
        if(onrejected == null) throw new IllegalArgumentException("Функция не может быть null");
        this.isSilentError = true;
        return new Promise<>(null,resolve -> {
            this.future.whenComplete((result, error) -> {
                if (error != null) {
                    try {
                        // Пытаемся восстановиться от ошибки
                        T recovered = onrejected.apply(error);
                        resolve.accept(recovered, null);
                    } catch (Exception e) {
                        resolve.accept(null, e);
                    }
                } else {
                    // Передаем результат дальше без изменений
                    resolve.accept(result, null);
                }
            });
        });
    }
    
    /**
     * Добавляет действие, выполняемое независимо от результата Promise.
     * @param action действие для выполнения
     * @return новый Promise с оригинальным результатом
     */
    public Promise<T> finallyDo(Runnable action) {
        return new Promise<>(null,resolve -> {
            this.future.whenComplete((result, error) -> {
                try {
                    // Выполняем действие независимо от результата
                    action.run();
                } finally {
                    // Передаем оригинальный результат или ошибку
                    if (error != null) {
                        resolve.accept(null, error);
                    } else {
                        resolve.accept(result, null);
                    }
                }
            });
        });
    }
    
    /**
     * Синхронно ожидает завершения Promise и возвращает результат.
     * @return результат Promise
     * @throws ExecutionException если Promise завершился с ошибкой
     * @throws InterruptedException если поток был прерван
     */
    public T awaitChecked() throws ExecutionException, InterruptedException {
        return future.get();
    }
    
    /**
     * Синхронно ожидает завершения Promise, оборачивая исключения в RuntimeException.
     * @return результат Promise
     * @throws RuntimeException если Promise завершился с ошибкой или был прерван
     */
    public T await() {
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Создает уже выполненный Promise с заданным значением.
     * @param <T> тип значения
     * @param value значение для Promise
     * @return выполненный Promise с значением
     */
    public static <T> Promise<T> resolve(T value) {
        return new Promise<>(null,resolve -> resolve.accept(value, null));
    }
    
    /**
     * Создает уже отклоненный Promise с заданной ошибкой.
     * @param <T> тип значения
     * @param error ошибка для Promise
     * @return отклоненный Promise с ошибкой
     */
    public static <T> Promise<T> reject(Throwable error) {
        return new Promise<>(null,resolve -> resolve.accept(null, error));
    }
    
    /**
     * Ожидает выполнения всех задач.
     * @param tasks массив задач для выполнения
     * @return Promise, который завершается когда все задачи выполнены
     */
    public static Promise<Void> all_run(Runnable... tasks) {
        return new Promise<>(null,resolve -> {
            CompletableFuture<?>[] futures = new CompletableFuture<?>[tasks.length];
            for (int i = 0; i < tasks.length; i++) {
                final int index = i;
                futures[i] = CompletableFuture.runAsync(tasks[index], virtualExecutor);
            }
            
            CompletableFuture.allOf(futures)
                .whenComplete((result, error) -> {
                    if (error != null) {
                        resolve.accept(null, error);
                    } else {
                        resolve.accept(null, null);
                    }
                });
        });
    }
    
    /**
     * Ожидает выполнения всех Promise и возвращает массив результатов.
     * @param <T> тип результатов Promise
     * @param promises массив Promise для ожидания
     * @return Promise с массивом результатов всех Promise
     */
    public static <T> Promise<java.util.List<T>> all(java.util.List<Promise<T>> promises) {
        return all(promises.toArray(Promise[]::new));
    }
    /**
     * Ожидает выполнения всех Promise и возвращает массив результатов.
     * @param <T> тип результатов Promise
     * @param promises массив Promise для ожидания
     * @return Promise с массивом результатов всех Promise
     */
    public static <T> Promise<java.util.List<T>> all(Promise<T>... promises) {
        return new Promise<>(null,resolve -> {
            @SuppressWarnings("unchecked")
            CompletableFuture<T>[] futures = new CompletableFuture[promises.length];
            for (int i = 0; i < promises.length; i++) {
                futures[i] = promises[i].future;
            }
            CompletableFuture.allOf(futures)
                .thenApply(v -> {
                    @SuppressWarnings("unchecked")
                    var results = new java.util.ArrayList<T>(promises.length);
                    for (int i = 0; i < promises.length; i++) {
                        results.add(i,futures[i].join());
                    }
                    return results;
                })
                .whenComplete((results, error) -> {
                    if (error != null) {
                        resolve.accept(null, error);
                    } else {
                        resolve.accept(results, null);
                    }
                });
        });
    }
}
