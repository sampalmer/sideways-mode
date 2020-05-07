package samuelpalmer.sensorautorotation.processes.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

/**
 * Monitors when the user or operating system unblocks the service notification.
 */
public class ServiceNotificationUnblockedMonitor extends EventRegistrarAdaptor {

    private static final String TAG = ServiceNotificationUnblockedMonitor.class.getSimpleName();

    private final Context context;
    private final IntentFilter filter;

    public ServiceNotificationUnblockedMonitor(Context context) {
        this.context = context;

        filter = new IntentFilter();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // This could only be monitored starting from Android 9.
            // Prior to this version, there is not way to know
            filter.addAction(NotificationManager.ACTION_APP_BLOCK_STATE_CHANGED);
            filter.addAction(NotificationManager.ACTION_NOTIFICATION_CHANNEL_BLOCK_STATE_CHANGED);
        }
    }

    @Override
    protected void subscribeToAdaptee() {
        context.registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        context.unregisterReceiver(broadcastReceiver);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (hasSubscriptions() && wasUnblocked(intent))
                report();
        }
    };

    /**
     * Determines whether the given intent represents the user or operating system unblocking the
     * service notification.
     */
    public static boolean wasUnblocked(Intent intent) {
        // Starting from Android 9, changes to notification blocking are broadcast. Before that,
        // we weren't notified.
        if (Build.VERSION.SDK_INT < 28)
            return false;

        @Nullable String action = intent.getAction();
        if (action != null) {
            if (NotificationManager.ACTION_APP_BLOCK_STATE_CHANGED.equals(action)) {
                boolean isBlocked = intent.getBooleanExtra(NotificationManager.EXTRA_BLOCKED_STATE, false);

                // If the app's notifications have just been unblocked, then we'll need to re-post the persistent notification.
                return !isBlocked;
            } else if (NotificationManager.ACTION_NOTIFICATION_CHANNEL_BLOCK_STATE_CHANGED.equals(action)) {
                boolean isBlocked = intent.getBooleanExtra(NotificationManager.EXTRA_BLOCKED_STATE, false);
                String channelId = intent.getStringExtra(NotificationManager.EXTRA_NOTIFICATION_CHANNEL_ID);

                // If the notification channel has just been unblocked, then we'll need to re-post the persistent notification
                return NotificationChannels.ID_CONTROLS.equals(channelId) && !isBlocked;
            }
        }

        Log.w(TAG, "Unknown action: " + action);
        return false;
    }

}
