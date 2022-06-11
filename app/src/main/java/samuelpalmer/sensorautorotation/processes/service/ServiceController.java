package samuelpalmer.sensorautorotation.processes.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import samuelpalmer.sensorautorotation.CurrentProcess;

/**
 * Interface to control the lifecycle of {@link RotationService}
 */
public abstract class ServiceController {
    
    public static void ensureStarted(Context context) {
        Intent intent = new Intent(context, RotationService.class);

        // Unfortunately, there's an OS bug that causes startService() to return null.
        // Maybe this retry logic will be enough to get by?
        int retries = 0;
        while (true) {
            ComponentName serviceComponentName;
            if (Build.VERSION.SDK_INT >= 26)
                serviceComponentName = context.startForegroundService(intent); // So the OS doesn't block the service start due to it being a "background" app
            else
                serviceComponentName = context.startService(intent);

            if (serviceComponentName != null)
                // We're good to go
                return;
            else {
                ++retries;
                if (retries < 3)
                    Log.w(ServiceController.class.getSimpleName(), "Service doesn't exist: " + intent.getComponent() + ". Retrying...");
                else
                    throw new RuntimeException("Service doesn't exist: " + intent.getComponent());
            }
        }
    }
    
    public static void ensureStopped(Context context) {
        if (!CurrentProcess.isServiceProcess())
            throw new RuntimeException("Cannot control the service from outside the service process");

        if (ServiceRunningStateHolder.isServiceRunning()) {
            // The service automatically stops its running state when it is destroyed.
            // But if we let this happen then the service notification will be removed by the OS, even if we tell it to preserve the notification, particularly in Android 10.
            // So we need to stop the running state earlier so it can detach from the notification before the OS begins shutting down the service.
            // See https://stackoverflow.com/questions/53302511/service-stopforegroundfalse-remove-notification-when-should-not
            ServiceRunningStateHolder.getServiceInstance().ensureStopped();
        }

        Intent intent = new Intent(context, RotationService.class);
        
        // It's important that we try to stop the service even if it doesn't look like it's running
        // since maybe the OS considers the service started, but it hasn't yet actually started it.
        context.stopService(intent);
    }
    
}
