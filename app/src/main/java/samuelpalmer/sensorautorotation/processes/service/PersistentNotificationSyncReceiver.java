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
 * This receiver updates the persistent notification in response to implicit broadcasts. For
 * updates triggered by things other than implicit broadcasts, see
 * {@link PersistentNotificationUpdater}.
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
            } else if (ServiceNotificationUnblockedMonitor.wasUnblocked(intent)) {
                // The notification has been unblocked, so we'll need to re-create it in order for
                // it to re-appear.
                return true;
            }

        Log.w(PersistentNotificationSyncReceiver.class.getSimpleName(), "Unknown action: " + action);
        return false;
    }

}
