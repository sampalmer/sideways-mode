package samuelpalmer.sensorautorotation.utilities.testing;

import android.util.Log;

import androidx.annotation.Nullable;

import java.io.Closeable;

/**
 * Use this to quickly measure how long a block of code takes to run and log the result
 * Use like this:
 *
 * <pre>
 * try (Stopwatch ignored = new Stopwatch("Doing something")) {
 *     yourCode();
 * }
 * </pre>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Stopwatch implements Closeable {

    private final long startNanos;
    private final String operation;
    private long stopNanos;
    private boolean hasStopNanos;

    /** Creates a new stopwatch and starts counting */
    public Stopwatch() {
        this(null);
    }

    /** Creates a new stopwatch with the given description and starts counting */
    public Stopwatch(@Nullable String operation) {
        startNanos = System.nanoTime();
        this.operation = operation == null ? "Operation" : operation;
    }

    @Override
    public void close() {
        if (hasStopNanos)
            throw new IllegalStateException("This stopwatch has already been used");

        stopNanos = System.nanoTime();
        hasStopNanos = true;

        String logMessage = operation + " took " + getElapsedMillis() + "ms";
        Log.i(getClass().getSimpleName(), logMessage);
    }

    public int getElapsedMillis() {
        long endNanos = hasStopNanos ? stopNanos : System.nanoTime();
        long elapsedNanos = endNanos - startNanos;
        double elapsedMicros = elapsedNanos / 1000d;
        double elapsedMillis = elapsedMicros / 1000d;
        return (int) Math.round(elapsedMillis);
    }

}
