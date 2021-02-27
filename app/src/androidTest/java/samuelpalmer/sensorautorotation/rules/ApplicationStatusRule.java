package samuelpalmer.sensorautorotation.rules;

import android.app.Instrumentation;
import android.content.Context;

import androidx.annotation.MainThread;
import androidx.annotation.Nullable;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.rules.ExternalResource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import samuelpalmer.sensorautorotation.processes.service.api.ApplicationStatusClient;
import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public class ApplicationStatusRule extends ExternalResource {

    private ApplicationStatusClient client;

    private volatile @Nullable ApplicationStatus status;

    private volatile @Nullable CountDownLatch receivedStatusChange;
    private final Object receivedStatusChangeLock = new Object();

    @Override
    protected void before() throws Throwable {
        super.before();

        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();

        // Using target context to increase chances of successfully binding to the service
        Context targetContext = instrumentation.getTargetContext();

        client = new ApplicationStatusClient(targetContext, listener);
        client.subscribe();
    }

    @Override
    protected void after() {
        super.after();

        client.unsubscribe();
        client = null;
    }

    /**
     * Gets the current status of the application. Will be <c>null</c> if the status is unavailable.
     */
    @Nullable
    public ApplicationStatus getStatus() {
        return status;
    }

    public void waitForStatusChange() {
        synchronized (receivedStatusChangeLock) {
            receivedStatusChange = new CountDownLatch(1);
        }

        boolean received;
        try {
            //noinspection ConstantConditions
            received = receivedStatusChange.await(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        synchronized (receivedStatusChangeLock) {
            receivedStatusChange = null;
        }

        if (!received)
            throw new RuntimeException("Didn't receive status change");
    }

    private final ApplicationStatusClient.Listener listener = new ApplicationStatusClient.Listener() {

        @Override
        @MainThread
        public void onChange(@Nullable ApplicationStatus status) {
            ApplicationStatusRule.this.status = status;

            synchronized (receivedStatusChangeLock) {
                if (receivedStatusChange != null) {
                    //noinspection ConstantConditions
                    receivedStatusChange.countDown();
                }
            }
        }

    };

}
