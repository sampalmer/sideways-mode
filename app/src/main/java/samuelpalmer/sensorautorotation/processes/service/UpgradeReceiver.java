package samuelpalmer.sensorautorotation.processes.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * This receiver ensures the service process will be created when the app is upgraded.
 * This allows the process to restart the service and persistent notification straight away since the OS has killed them.
 */
public class UpgradeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(UpgradeReceiver.class.getSimpleName(), "Received intent: " + intent.getAction());
        
        if (Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
            SharedPreferences prefs = ServiceSettings.getSharedPreferences(context);
            boolean wasRunning = prefs.contains(ServiceSettings.KEY_PERSISTENT_STATE);
            
            // Only starting the service when absolutely necessary in case this is the cause of Android occasionally creating multiple simultaneous instances of the service.
            if (wasRunning) {
                /*
                When Android updates an app, it kills the app's processes and doesn't restart the service.
                So we need to restart it ourselves.
                
                Note that it's also possible that the app crashed a long time ago, leaving behind its state, and was never re-run.
                In that situation, the service shouldn't be started, but I haven't come up with a good way of detecting that situation yet.
                */
                ServiceController.ensureStarted(context);
            }

            // Each time an app is upgraded, the OS removes all its notifications, so we need to re-create them if necessary
            PersistentNotificationUpdater.sync(context);
        }
        else
            Log.w(UpgradeReceiver.class.getSimpleName(), "Unknown action: " + intent.getAction());
    }

}
