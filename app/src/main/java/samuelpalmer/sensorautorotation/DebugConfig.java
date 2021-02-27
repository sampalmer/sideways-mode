package samuelpalmer.sensorautorotation;

import android.os.StrictMode;

public final class DebugConfig {

    /**
     * Applies configuration to the current process to assist local development and debugging.
     * This should not be run in Production.
     */
    static void configureDebug() {
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
            .detectAll()
            .permitDiskReads() // So we can read SharedPreferences
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
    }

}
