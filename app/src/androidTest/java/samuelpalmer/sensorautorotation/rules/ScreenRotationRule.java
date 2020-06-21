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
    private CountDownLatch waitForChangeLatch;

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
            // WARNING: We are not in the same thread as the test here
            waitForChangeLatch.countDown();
        }
    };

    /**
     * Waits for the screen orientation to change and then returns the resulting screen orientation.
     * If the screen is already in the expected rotation, then returns immediately.
     */
    public Orientation waitForRotation(Orientation expectedRotation) {
        // If the screen is already in the expected rotation then we'd better stop since the OS
        // won't notify us of a rotation change since there is none!
        Orientation currentRotation = watcher.lastRotationOrNull();
        if (currentRotation == expectedRotation)
            return currentRotation;

        waitForChangeLatch = new CountDownLatch(1);

        boolean waitResult;
        try {
            waitResult = waitForChangeLatch.await(5000, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            waitForChangeLatch = null;
        }

        if (!waitResult)
            throw new RuntimeException("Timed out waiting for screen rotation to change");

        return watcher.lastRotationOrNull();
    }
}
