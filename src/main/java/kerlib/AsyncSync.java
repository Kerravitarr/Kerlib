///
/// Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
/// Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
///

package kerlib;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import kerlib.jpromise.Promise;

///Коалесцирующий семафор для «тяжёлых» блокирующих операций.
///На каждое имя одновременно выполняется только один запуск.
///Если вызвать повторно во время выполнения — параметры запоминаются,
///и по завершении текущего прогона функция будет вызвана ещё раз
///с последними параметрами.
///
/// @param <K> тип ключей, которые сюда попадают. Но можно и просто Object оставить
///
/// @author Ilia Pushkin (github.com/Kerravitarr)
public class AsyncSync<K> {
    ///Карта. По каждой функции - свой вызов
    private final ConcurrentHashMap<K, State> map = new ConcurrentHashMap<>();
    
    
    public void semaphore(K key, Runnable fn){
        semaphore(key, null, nil -> fn.run());
    }
    public <ARG1> void semaphore(K key,ARG1 arg, Consumer<ARG1> fn){
        semaphore(key, new Object[]{arg}, args -> {
            fn.accept((ARG1) args[0]);
        });
    }
    
    public <ARG1,ARG2> void semaphore(K key,ARG1 arg1,ARG2 arg2, BiConsumer<ARG1,ARG2> fn){
        semaphore(key, new Object[]{arg1,arg2}, args -> {
            fn.accept((ARG1) args[0],(ARG2) args[1]);
        });
    }
            
    ///Запустить {@code fn} (возвращающую Promise) с коалесцировкой по ключу.
    ///Потоки для исполнения предоставляет сам Promise (virtualExecutor внутри),
    /// @param key ключ, по которому мы отделяем разные функции друг от друга
    /// @param args аргументы функции, которые будут переданы в неё
    /// @param fn функция, которая будет выполнена
    public void semaphore(K key, Object[] args, Consumer<Object[]> fn) {
        boolean start = false;
        long version = -1;
        Object[] startArgs = null;

        synchronized (map) {
            var s = map.get(key);
            if (s == null) {
                var fresh = new State(args, 1);
                map.put(key, fresh);
                start = true;
                version = fresh.version;
                startArgs = args;
            } else {
                s.params = args;
                s.version++;
                if (!s.running) {                  // предыдущий прогон уже завершился
                    s.running = true;
                    start = true;
                    version = s.version;
                    startArgs = s.params;
                }
            }
        }

        if (start) {
            runLoop(key, startArgs, version, fn);
        }
    }
    ///Кольцо выполнения. В нём и будет выполняться функция
    private void runLoop(K key, Object[] args, long myVersion, Consumer<Object[]> fn) {
        // Обёртка: fn запускается на виртуальном потоке внутри Promise.
        // try/catch вокруг fn.accept нужен только чтобы явно превратить
        // исключение в reject (иначе Promise поймает его и так, но с другим стеком).
        var p = new Promise<Void>((resolve, reject) -> {
            try {
                fn.accept(args);
            } catch (Throwable t) {
                reject.accept(t);
                return;                 // reject.accept бросает PromiseEnd — сюда не дойдём,
            }                           // но компилятор этого не знает
            resolve.accept(null);
        });
        // finallyDo — точный аналог try/finally в TS-оригинале:
        // срабатывает и на resolve, и на reject, ровно один раз.
        p.finallyDo(() -> onComplete(key, myVersion, fn));
    }
    ///Проверяет результат завершения. Если у нас изменились входные данные, то будет ещё один цикл вызова
    private void onComplete(K key, long myVersion, Consumer<Object[]> fn) {
        Object[] nextArgs;
        long nextVersion;
        synchronized (map) {
            State s = map.get(key);
            if (s == null) return;
            if (s.version > myVersion) {
                nextArgs = s.params;
                nextVersion = s.version;
            } else {
                s.running = false;
                map.remove(key);
                return;
            }
        }
        runLoop(key, nextArgs, nextVersion, fn);
    }
  
    
    ///Состояние автомата, которое и вызывает функцию один раз
    private static final class State {
        ///Параметры вызова
        Object[] params;
        ///Количество вызовов
        long version;
        ///Мы работаем?
        boolean running;
        
        State(Object[] params, long version) {
            this.params = params;
            this.version = version;
            this.running = true;
        }
    }
}
