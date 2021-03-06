package samuelpalmer.sensorautorotation.rules.notification;

import android.os.Handler;
import android.os.Looper;
import android.service.notification.StatusBarNotification;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.os.HandlerCompat;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.rules.ExternalResource;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import samuelpalmer.sensorautorotation.components.TestingNotificationListener;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrar;

public class NotificationRule extends ExternalResource {

    private final ActivityScenarioRule<? extends AppCompatActivity> activity;
    private final Object latchLock = new Object();
    private CountDownLatch waitForChangeLatch;
    private final Handler mainHandler = HandlerCompat.createAsync(Looper.getMainLooper());

    private NotificationListenerPermissionManager permissionManager;

    public NotificationRule(ActivityScenarioRule<? extends AppCompatActivity> activity) {
        this.activity = activity;
    }

    public StatusBarNotification[] waitForNotificationsToSettle() {
        // Wait for notifications to settle
        try {
            while (true) {
                synchronized (latchLock) {
                    waitForChangeLatch = new CountDownLatch(1);
                }

                try {
                    boolean notificationsChanged = waitForChangeLatch.await(1000, TimeUnit.MILLISECONDS);
                    if (!notificationsChanged)
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

        // Get final set of notifications
        final StatusBarNotification[][] notificationsHolder = new StatusBarNotification[1][1];
        CountDownLatch gotNotifications = new CountDownLatch(1);
        mainHandler.post(() -> {
            notificationsHolder[0] = TestingNotificationListener.getNotifications();
            gotNotifications.countDown();
        });
        try {
            boolean success = gotNotifications.await(5000, TimeUnit.MILLISECONDS);
            if (!success)
                throw new RuntimeException("Took too long to get notifications");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return notificationsHolder[0];
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        permissionManager = NotificationListenerPermissionManager.create(activity);

        permissionManager.grantAccess();
        if (!permissionManager.isEnabled())
            throw new RuntimeException("Notification listener is not enabled");

        waitForListenerConnection(TestingNotificationListener.onConnected, true);

        // Subscribe to changes
        CountDownLatch subscribed = new CountDownLatch(1);
        mainHandler.post(() -> {
            TestingNotificationListener.onNotificationsChanged.subscribe(notificationsChangeHandler);
            subscribed.countDown();
        });
        boolean success = subscribed.await(5000, TimeUnit.MILLISECONDS);
        if (!success)
            throw new RuntimeException("Took too long to subscribe to notifications");
    }

    @Override
    protected void after() {
        super.after();

        // Unsubscribe from changes
        CountDownLatch unsubscribed = new CountDownLatch(1);
        mainHandler.post(() -> {
            TestingNotificationListener.onNotificationsChanged.unsubscribe(notificationsChangeHandler);
            unsubscribed.countDown();
        });
        try {
            boolean success = unsubscribed.await(5000, TimeUnit.MILLISECONDS);
            if (!success)
                throw new RuntimeException("Took too long to unsubscribe from notifications");
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        permissionManager.revokeAccess();
        if (permissionManager.isEnabled())
            throw new RuntimeException("Notification listener is enabled");

        permissionManager = null;

        waitForListenerConnection(TestingNotificationListener.onDisconnected, false);
    }

    private final EventHandler notificationsChangeHandler = new EventHandler() {
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

    private void waitForListenerConnection(EventRegistrar event, boolean targetState) {
        try {
            // Broadcasts and events occur in the main thread, so we'll have to listen over there
            CountDownLatch targetStateReached = new CountDownLatch(1);
            EventHandler notifyTargetStateReached = targetStateReached::countDown;

            // Subscribe to event
            CountDownLatch subscribed = new CountDownLatch(1);
            mainHandler.post(() -> {
                event.subscribe(notifyTargetStateReached);
                subscribed.countDown();

                // We might have already reached the target state before we got here
                if (TestingNotificationListener.isConnected() == targetState)
                    targetStateReached.countDown();
            });
            if (!subscribed.await(5, TimeUnit.SECONDS))
                throw new RuntimeException("Took too long to subscribe to notification listener connection state " + targetState);

            // Wait for target state
            try {
                if (!targetStateReached.await(10, TimeUnit.SECONDS))
                    throw new RuntimeException("Took too long to reach notification listener connection state " + targetState);
            } finally {
                // Unsubscribe from event
                CountDownLatch unsubscribed = new CountDownLatch(1);
                mainHandler.post(() -> {
                    event.unsubscribe(notifyTargetStateReached);
                    unsubscribed.countDown();
                });
                if (!unsubscribed.await(5, TimeUnit.SECONDS))
                    //noinspection ThrowFromFinallyBlock
                    throw new RuntimeException("Took too long to unsubscribe from notification listener connection state " + targetState);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
