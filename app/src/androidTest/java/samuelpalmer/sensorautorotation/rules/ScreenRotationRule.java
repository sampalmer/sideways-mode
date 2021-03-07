package samuelpalmer.sensorautorotation.rules;

import org.junit.rules.ExternalResource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import samuelpalmer.sensorautorotation.processes.service.screen.RotationWatcherRegistrar;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.values.Orientation;

/**
 * Convenience methods for screen orientation
 */
public class ScreenRotationRule extends ExternalResource {

    private RotationWatcherRegistrar watcher;

    private volatile CountDownLatch waitForChangeLatch;
    private final Object latchLock = new Object();

    @Override
    protected void before() throws Throwable {
        super.before();

        watcher = RotationWatcherRegistrar.instance();
        watcher.subscribe(rotationChanged);
    }

    @Override
    protected void after() {
        super.after();

        watcher.unsubscribe(rotationChanged);
        watcher = null;
    }

    private final EventHandler rotationChanged = new EventHandler() {
        @Override
        public void update() {
            synchronized (latchLock) {
                if (waitForChangeLatch == null)
                    return; // We're not currently waiting for a change, so nothing to do

                // WARNING: We are not in the same thread as the test here
                waitForChangeLatch.countDown();
            }
        }
    };

    /**
     * Waits for the screen orientation to stop changing, and then returns the resulting orientation
     */
    public Orientation waitForScreenToSettle() {
        try {
            while (true) {
                synchronized (latchLock) {
                    waitForChangeLatch = new CountDownLatch(1);
                }

                try {
                    boolean rotationChanged = waitForChangeLatch.await(4000, TimeUnit.MILLISECONDS);
                    if (!rotationChanged)
                        break;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            synchronized (latchLock) {
                waitForChangeLatch = null;
            }
        }

        return watcher.lastRotationOrNull();
    }
}
