package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * This receiver creates the persistent notification when the operating system boots.
 */
public class OsBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(OsBootReceiver.class.getSimpleName(), "Received intent: " + intent.getAction());
        
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // The user has just turned on their device, so the notification won't be present.
            // So we'll re-create it if appropriate.
            PersistentNotification.sync(context);
        }
        else
            Log.w(OsBootReceiver.class.getSimpleName(), "Unknown action: " + intent.getAction());
    }

}
