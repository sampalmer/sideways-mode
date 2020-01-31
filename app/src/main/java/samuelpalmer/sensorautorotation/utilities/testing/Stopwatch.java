package samuelpalmer.sensorautorotation.utilities.testing;

@SuppressWarnings("unused")
public class Stopwatch {

    private long startNanos;
    private boolean hasStartNanos;

    private long stopNanos;
    private boolean hasStopNanos;

    public static Stopwatch startNew() {
        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        return stopwatch;
    }

    public void start() {
        if (hasStartNanos)
            throw new IllegalStateException("Already started");

        if (hasStopNanos)
            throw new IllegalStateException("This stopwatch has already been used");

        startNanos = System.nanoTime();
        hasStartNanos = true;
    }

    public void stop() {
        if (!hasStartNanos)
            throw new IllegalStateException("Not started");

        if (hasStopNanos)
            throw new IllegalStateException("This stopwatch has already been used");

        stopNanos = System.nanoTime();
        hasStopNanos = true;
    }

    public int getElapsedMillis() {
        if (!hasStartNanos)
            throw new IllegalStateException("Not started");

        long endNanos = hasStopNanos ? stopNanos : System.nanoTime();
        long elapsedNanos = endNanos - startNanos;
        double elapsedMicros = elapsedNanos / 1000d;
        double elapsedMillis = elapsedMicros / 1000d;
        return (int) Math.round(elapsedMillis);
    }

}
