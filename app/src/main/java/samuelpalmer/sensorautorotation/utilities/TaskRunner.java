package samuelpalmer.sensorautorotation.utilities;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * Runs a task on a background thread and posts the result to the main thread.
 * Based on code from https://stackoverflow.com/a/58767934/238753.
 */
public class TaskRunner {

    private final ExecutorService executor;
    private final Handler handler;

    public TaskRunner(ExecutorService executor) {
        this.executor = executor;
        handler = new Handler(Looper.getMainLooper());
    }

    public interface Callback<T extends Callable<R>, R> {
        void onComplete(T task, R result);
    }

    public <T extends Callable<R>, R> Future<?> executeAsync(T callable, Callback<T, R> callback) {
        return executor.submit(() -> {
            try {
                R result = callable.call();
                handler.post(() -> callback.onComplete(callable, result));
            } catch (Exception e) {
                handler.post(() -> {
                    throw new RuntimeException(e);
                });
            }
        });
    }

}
