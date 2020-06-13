package samuelpalmer.sensorautorotation;

import android.app.AppOpsManager;
import android.content.Context;
import android.os.Build;
import android.os.StrictMode;

import samuelpalmer.sensorautorotation.utilities.LoggingAppOpsCollector;

public final class DebugConfig {

    // Whether to audit usage of dangerous permissions. This generates a lot of log noise, so it's off by default.
    private static final boolean AUDIT_PERMISSIONS = false;

    /**
     * Applies configuration to the current process to assist local development and debugging.
     * This should not be run in Production.
     */
    static void configureDebug(Context context) {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .permitDiskReads() // So we can read SharedPreferences and the current process name
            .permitDiskWrites() // So SharedPreferences can initialise itself on first-run
            .penaltyLog()
            // Not using penaltyDialog since it's too annoying
            .build()
        );

        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
            .detectAll()
            .penaltyLog()
            // Not using penaltyDeath since it makes it look like the app is malfunctioning
            .build()
        );

        if (AUDIT_PERMISSIONS) {
            if (Build.VERSION.SDK_INT >= 30) {
                // Logging all uses of dangerous app ops to make sure no privacy-breaching code makes it to production
                AppOpsManager appOpsManager = context.getSystemService(AppOpsManager.class);
                assert appOpsManager != null;
                appOpsManager.setOnOpNotedCallback(context.getMainExecutor(), new LoggingAppOpsCollector());
            }
        }
    }

}
