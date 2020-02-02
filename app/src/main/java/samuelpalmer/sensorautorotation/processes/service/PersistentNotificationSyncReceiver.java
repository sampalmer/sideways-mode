package samuelpalmer.sensorautorotation.processes.service;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import samuelpalmer.sensorautorotation.notifications.NotificationChannels;

/**
 * This receiver creates the persistent notification when the operating system boots.
 */
public class PersistentNotificationSyncReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(PersistentNotificationSyncReceiver.class.getSimpleName(), "Received intent: " + intent.getAction());
        
        if (syncNeeded(intent))
            PersistentNotificationUpdater.sync(context);
    }

    private boolean syncNeeded(Intent intent) {
        @Nullable String action = intent.getAction();

        if (action != null)
            if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                // The user has just turned on their device, so the notification won't be present.
                // So we'll re-create it if appropriate.
                return true;
            } else if (Build.VERSION.SDK_INT >= 28) {
                // Android 9+ broadcasts changes to notification blocking

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

        Log.w(PersistentNotificationSyncReceiver.class.getSimpleName(), "Unknown action: " + action);
        return false;
    }

}
