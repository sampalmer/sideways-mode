package samuelpalmer.sensorautorotation.rules;

import android.content.Context;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.ExternalResource;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import samuelpalmer.sensorautorotation.processes.service.api.ApplicationStatusClient;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

/**
 * Provides direct control of the app using {@link ApplicationCommand}
 */
public class ApplicationRule extends ExternalResource {

    private Context targetContext;
    private ApplicationStatusClient client;
    private @Nullable ApplicationStatus lastStatus;
    private LinkedBlockingQueue<ApplicationStatus> statusQueue;

    @Override
    protected void before() throws Throwable {
        super.before();

        statusQueue = new LinkedBlockingQueue<>();

        // Must use target context to send commands rather than our own context so that the commands
        // are received by the app
        targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // Using target context to increase chances of successfully binding to the service
        client = new ApplicationStatusClient(targetContext, listener, Looper.getMainLooper());
        client.subscribe();
    }

    @Override
    protected void after() {
        super.after();

        refreshLastStatus();
        if (lastStatus == null || lastStatus.runningState != null)
            stopApp();

        clearEnvironmentErrors();

        targetContext = null;

        client.unsubscribe();
        client = null;
    }

    /**
     * Starts the app with the given {@link ApplicationCommand}
     * @param action either {@link ApplicationCommand#ACTION_LEFT_SIDE} or {@link ApplicationCommand#ACTION_RIGHT_SIDE}
     * @return the resulting status after the app has finished starting up
     */
    public ApplicationStatus startApp(@ApplicationCommand.ACTION String action) {
        if (!action.equals(ApplicationCommand.ACTION_LEFT_SIDE) && !action.equals(ApplicationCommand.ACTION_RIGHT_SIDE))
            throw new IllegalArgumentException("Cannot start app with command " + action);

        refreshLastStatus();

        if (lastStatus != null && lastStatus.runningState != null)
            throw new IllegalStateException("App is already running");

        // Sending start command
        ApplicationCommand command = new ApplicationCommand(action);
        command.send(targetContext);

        // Waiting for startup to complete
        try {
            return waitForAppToStart();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Stops the app and waits for it to shutdown
     */
    public void stopApp() {
        refreshLastStatus();

        if (lastStatus != null && lastStatus.runningState == null)
            throw new IllegalStateException("App is already stopped");

        // Sending stop command
        ApplicationCommand command = new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF);
        command.send(targetContext);

        try {
            waitForAppToStop();
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Gets the most up-to-date status, waiting if necessary for the connection to the listener to
     * establish.
     */
    public ApplicationStatus getCurrentStatus() {
        refreshLastStatus();

        if (lastStatus == null) {
            try {
                lastStatus = statusQueue.poll(5000, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException();
            }

            if (lastStatus == null)
                throw new RuntimeException("Took too long to get the first status");
        }

        return lastStatus;
    }

    private void clearEnvironmentErrors() {
        ApplicationStatus originalStatus = getCurrentStatus();
        if (originalStatus.error == null)
            return;

        ApplicationCommand clearErrorCommand = new ApplicationCommand(ApplicationCommand.ACTION_CLEAR_ERROR);
        clearErrorCommand.send(targetContext);

        try {
            waitForAppState(status -> status.error == null, "error cleared");
        } catch (TimeoutException e) {
            throw new RuntimeException("Took too long to clear environment error", e);
        }
    }

    /** Sets {@link #lastStatus} to the most recent status received by the listener */
    private void refreshLastStatus() {
        while (true) {
            ApplicationStatus newStatus = statusQueue.poll();
            if (newStatus == null)
                break;

            lastStatus = newStatus;
        }
    }

    public ApplicationStatus waitForAppToStart() throws TimeoutException {
        return waitForAppState(status -> status.runningState != null && !status.runningState.waitingForSensor, "started");
    }

    public ApplicationStatus waitForAppToStop() throws TimeoutException {
        return waitForAppState(status -> status.runningState == null, "stopped");
    }

    /**
     * Waits for the app to reach the given running state. Note that if the status has already
     * changed since the last call to any method on this rule, then that previous status change will
     * also be considered.
     *
     * @return The target running state
     * @throws TimeoutException if the target running state is not reached
     */
    private ApplicationStatus waitForAppState(TargetAppState targetState, String targetStateName) throws TimeoutException {
        long timeout = System.currentTimeMillis() + 5000;
        while (true) {
            ApplicationStatus newStatus;
            try {
                newStatus = statusQueue.poll(timeout - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            if (newStatus == null)
                throw new TimeoutException("Took too long for app to reach state " + targetStateName);

            lastStatus = newStatus;
            if (targetState.hasReachedTargetState(newStatus))
                break;
        }

        return lastStatus;
    }

    private final ApplicationStatusClient.Listener listener = new ApplicationStatusClient.Listener() {

        @Override
        @MainThread
        public void onChange(@Nullable ApplicationStatus status) {
            if (status == null)
                // We got disconnected, so we'll just ignore until we're reconnected
                return;

            try {
                statusQueue.put(status);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    };

    private interface TargetAppState {
        boolean hasReachedTargetState(ApplicationStatus status);
    }

}
