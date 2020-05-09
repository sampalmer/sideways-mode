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

    public interface Callback<R> {
        void onComplete(R result);
    }

    public <R> Future<?> executeAsync(Callable<R> callable, Callback<R> callback) {
        return executor.submit(() -> {
            try {
                R result = callable.call();
                handler.post(() -> callback.onComplete(result));
            } catch (Exception e) {
                handler.post(() -> {
                    throw new RuntimeException(e);
                });
            }
        });
    }

}
