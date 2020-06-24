package samuelpalmer.sensorautorotation.rules;

import androidx.annotation.Nullable;

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
            if (waitForChangeLatch == null)
                return; // We're not currently waiting for a change, so nothing to do

            // WARNING: We are not in the same thread as the test here
            waitForChangeLatch.countDown();
        }
    };

    /**
     * Waits for the screen orientation to change and then returns the resulting screen orientation.
     * If the screen is already in the expected rotation, then returns immediately.
     */
    public Orientation waitForRotation(@Nullable Orientation expectedRotation) {
        // If the screen is already in the expected rotation then we'd better stop since the OS
        // won't notify us of a rotation change since there is none!
        Orientation currentRotation = watcher.lastRotationOrNull();
        if (currentRotation == expectedRotation)
            return currentRotation;

        waitForChangeLatch = new CountDownLatch(1);

        try {
            waitForChangeLatch.await(3500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            waitForChangeLatch = null;
        }

        // Even if we timed out, then we'll still proceed since the OS's screen rotation must be settled by now

        return watcher.lastRotationOrNull();
    }
}
