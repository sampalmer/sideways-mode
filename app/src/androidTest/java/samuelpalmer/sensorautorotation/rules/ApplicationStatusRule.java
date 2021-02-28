package samuelpalmer.sensorautorotation.rules;

import android.app.Instrumentation;
import android.content.Context;
import android.os.Looper;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
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

        client = new ApplicationStatusClient(targetContext, listener, Looper.getMainLooper());
        client.subscribe();
    }

    @Override
    protected void after() {
        super.after();

        client.unsubscribe();
        client = null;
    }

    @NonNull
    public ApplicationStatus waitForStatusChange() {
        try {
            while (true) {
                synchronized (receivedStatusChangeLock) {
                    receivedStatusChange = new CountDownLatch(1);
                }

                try {
                    boolean statusChanged = receivedStatusChange.await(1000, TimeUnit.MILLISECONDS);
                    if (status != null && !statusChanged)
                        break;
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        } finally {
            synchronized (receivedStatusChangeLock) {
                receivedStatusChange = null;
            }
        }

        return status;
    }

    private final ApplicationStatusClient.Listener listener = new ApplicationStatusClient.Listener() {

        @Override
        @MainThread
        public void onChange(@Nullable ApplicationStatus status) {
            if (status == null)
                // We got disconnected, so we'll just ignore until we're reconnected
                return;

            synchronized (receivedStatusChangeLock) {
                ApplicationStatusRule.this.status = status;

                if (receivedStatusChange != null) {
                    //noinspection ConstantConditions
                    receivedStatusChange.countDown();
                }
            }
        }

    };

}
