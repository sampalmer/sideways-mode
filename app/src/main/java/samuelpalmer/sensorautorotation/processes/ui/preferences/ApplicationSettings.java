package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import samuelpalmer.sensorautorotation.CurrentProcess;

/**
 * The app's main {@link SharedPreferences} file. For use by the UI process only.
 * This is intentionally kept simple so that {@link SharedPreferences} access is consistent with
 * other apps.
 */
public abstract class ApplicationSettings {

    public static SharedPreferences getSharedPreferences(Context context) {
        if (CurrentProcess.isServiceProcess(context))
            throw new RuntimeException("Application settings accessed outside of the UI process");
        
        return context.getSharedPreferences(getSharedPreferencesName(context), Context.MODE_PRIVATE);
    }

    public static String getSharedPreferencesName(Context context) {
        if (CurrentProcess.isServiceProcess(context))
            throw new RuntimeException("Application settings accessed outside of the UI process");

        return context.getPackageName() + "_preferences";
    }

    /*
     * NOTE: If you change preference config:
     * 1. Add a migration to the "Migrations" class
     * 2. If you're altering a user preference, ensure the changes are synced to the service process
     */
    
    /**
     * Used for upgrading settings data when upgrading to a newer version of the app.
     */
    public static final String KEY_APP_VERSION = "appVersion";
    
    public static final String KEY_INCOMPATIBLE_APP_PACKAGES = "incompatibleAppPackages";

    /**
     * Whether the app should stop running each time the user locks the device.
     * The user will need to manually turn it back on if they want to use it again.
     */
    public static final String KEY_TURN_OFF_WHEN_LOCKED = "turnOffWhenLocked";

    /**
     * Whether the notification should show when the app is in the "off" state.
     * This provides faster access to the app at the expense of space in the OS notification shade.
     */
    public static final String KEY_CLOSE_NOTIFICATION_WHEN_UPRIGHT = "closeNotificationWhenUpright";

    /**
     * Whether the user has been told that the app is running in the background.
     */
    public static final String KEY_RUNNING_IN_BACKGROUND_TIP_SHOWN = "runningInBackgroundTipShown";

    /**
     * Whether the user has been told how to start the app.
     */
    public static final String KEY_TURNING_ON_TIP_SHOWN = "turningOnTipShown";

    /**
     * Whether the user has been told when to use the incompatible apps feature.
     */
    public static final String KEY_INCOMPATIBLE_APPS_TIP_SHOWN = "incompatibleAppsTipShown";

    /**
     * Whether the user has been told to turn off the app before uninstalling it.
     */
    public static final String KEY_UNINSTALLING_TIP_SHOWN = "uninstallingTipShown";

}
