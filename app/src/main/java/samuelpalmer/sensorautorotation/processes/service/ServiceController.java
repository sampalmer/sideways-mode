package samuelpalmer.sensorautorotation.processes.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

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
        Intent intent = new Intent(context, RotationService.class);
        
        // It's important that we try to stop the service even if it doesn't look like it's running
        // since maybe the OS considers the service started, but it hasn't yet actually started it.
        context.stopService(intent);
    }
    
}
