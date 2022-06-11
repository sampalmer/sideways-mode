package samuelpalmer.sensorautorotation.utilities;

/**
 * An application logic component that has a simple lifecycle consisting of starting and stopping
 */
public abstract class State {
    private boolean started;

    public void ensureStarted() {
        if (!started) {
            start();
            started = true;
        }
    }

    public void ensureStopped() {
        if (started) {
            stop();
            started = false;
        }
    }

    public boolean isStarted() {
        return started;
    }

    /**
     * Throws an exception if {@link #isStarted()} is {@code false}
     */
    protected void verifyStarted() {
        if (!isStarted())
            throw new RuntimeException("Not started");
    }

    protected abstract void start();

    protected abstract void stop();
}
