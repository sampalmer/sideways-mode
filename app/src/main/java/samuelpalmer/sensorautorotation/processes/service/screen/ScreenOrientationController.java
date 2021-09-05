package samuelpalmer.sensorautorotation.processes.service.screen;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.Surface;

import androidx.annotation.Nullable;
import androidx.core.os.HandlerCompat;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.errors.GenericError;
import samuelpalmer.sensorautorotation.processes.service.EnvironmentErrorManager;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.utilities.AlwaysGrantedPermissionMonitor;
import samuelpalmer.sensorautorotation.utilities.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.utilities.AppOpMonitor;
import samuelpalmer.sensorautorotation.utilities.PermissionMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.values.Orientation;

public class ScreenOrientationController extends State {

    private static final String TAG = ScreenOrientationController.class.getSimpleName();

    // Keeping this pretty low since Android P could theoretically change the screen orientation quite quickly if the
    // user mashes the Recents button to rapidly switch between a landscape app and a portrait app
    private static final float MAX_EXTERNAL_ROTATIONS_PER_SECOND = .5f;

    public static PermissionMonitor makePermissionMonitor(Context context) {
        // Using App Ops instead of Settings.System.canWrite() to ensure permission polling is consistent with notifications from the OS.
        // This also makes automated tests of these features more reliable.
        if (Build.VERSION.SDK_INT >= 23)
            return new AppOpMonitor(context, AppOpsManager.OPSTR_WRITE_SETTINGS);
        else
            return new AlwaysGrantedPermissionMonitor();
    }

    private final Context context;
    private final ScreenRotationMonitor screen;
    private final ContentResolver contentResolver;
    private final PermissionMonitor systemSettingPermissionMonitor;
    private final SharedPreferences prefs;

    @Nullable
    private Orientation lastProposed;

    private ExternalRotationChangeRateLimiter rateLimiter;

    public ScreenOrientationController(Context context, ScreenRotationMonitor screen) {
        this.context = context;
        this.screen = screen;
        contentResolver = context.getContentResolver();
        systemSettingPermissionMonitor = makePermissionMonitor(context);
        prefs = ServiceSettings.getSharedPreferences(context);
    }

    @Override
    protected void start() {
        lastProposed = null;
        rateLimiter = new ExternalRotationChangeRateLimiter();

        boolean wasAutoRotationOn = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 1;

        if (!prefs.contains(ServiceSettings.KEY_SYSTEM_ROTATION)) {
            // It's possible that we previously crashed and already have the value.
            // In this case, we don't want to re-fetch it since this app will have already turned system auto rotation off and we won't be able to determine the setting's original state.
            // Storing this value so we can restore the original value when this app is stopped.
            prefs
                    .edit()
                    .putBoolean(ServiceSettings.KEY_SYSTEM_ROTATION, wasAutoRotationOn)
                    .apply();
        }

        if (wasAutoRotationOn) {
            Orientation realOrientation = screen.currentRotation();
            propose(realOrientation); // To prevent the screen orientation from switching after we turn off auto-rotation
            
            setAndHandleErrors(Settings.System.ACCELEROMETER_ROTATION, 0); // Since it's incompatible with this app
            propose(realOrientation); // Just in case disabling system rotation reset this.
        }
        
        contentResolver.registerContentObserver(Settings.System.getUriFor(Settings.System.USER_ROTATION), false, onUserRotationChanged);
        systemSettingPermissionMonitor.subscribe(systemSettingPermissionChanged);
    }

    @Override
    protected void stop() {
        systemSettingPermissionMonitor.unsubscribe(systemSettingPermissionChanged);
        contentResolver.unregisterContentObserver(onUserRotationChanged);

        // Because the user is using this app, we can assume that they want to control the screen orientation.
        // Before Android P, the user can only change the screen orientation in auto-rotate mode, so we'll assume they want that mode.
        // Starting from Android P, both rotation lock mode and auto-rotate mode let the user control the screen orientation,
        // so we'll restore the mode that they were using when they started this app.
        boolean useSystemRotation = Build.VERSION.SDK_INT < 28 || wasSystemRotationOn();
        setAndHandleErrors(Settings.System.ACCELEROMETER_ROTATION, useSystemRotation ? 1 : 0);

        // The OS usually has this set to natural orientation, so we'll follow that convention to keep things in a clean state.
        // This also ensures that, if the user is in locked screen rotation mode, it goes back to the device's natural orientation.
        propose(Orientation.NATURAL);
        prefs.edit().remove(ServiceSettings.KEY_SYSTEM_ROTATION).apply();
    }

    public void propose(Orientation value) {
        if (value == null)
            throw new IllegalArgumentException();

        Log.i(TAG, "Proposing screen orientation: " + value);
        
        boolean worked = setAndHandleErrors(Settings.System.USER_ROTATION, AndroidSurfaceRotation.fromOrientationClockwise(value));
        if (worked)
            lastProposed = value;
    }

    public boolean wasSystemRotationOn() {
        if (!prefs.contains(ServiceSettings.KEY_SYSTEM_ROTATION))
            throw new IllegalStateException("This setting is not available in this state.");

        return prefs.getBoolean(ServiceSettings.KEY_SYSTEM_ROTATION, true);
    }

    private boolean setAndHandleErrors(String name, int newValue) {
        if (!validateSystemSettingsPermission())
            return false;

        boolean worked = Settings.System.putInt(contentResolver, name, newValue);
        if (!worked) {
            // Because the developer can't do anything about this; it seems to be a problem on the device.
            GenericError error = new GenericError(context, R.string.environment_error_system_setting_write_failure_summary, context.getString(R.string.environment_error_system_setting_write_failure_message, name, Integer.toString(newValue)));
            EnvironmentErrorManager.report(context, error);
            return false;
        }
        
        return true;
    }

    private final ContentObserver onUserRotationChanged = new ContentObserver(HandlerCompat.createAsync(Looper.getMainLooper())) {
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            
            if (isStarted() && lastProposed != null) {
                int newUserRotation = Settings.System.getInt(contentResolver, Settings.System.USER_ROTATION, Surface.ROTATION_0);
                Orientation newOrientation = AndroidSurfaceRotation.toOrientationClockwise(newUserRotation);
                if (!newOrientation.equals(lastProposed)) {
                    rateLimiter.registerExternalRotationChange();
                    Float externalRotationChangesPerSecond = rateLimiter.getExternalRotationChangesPerSecond();

                    if (externalRotationChangesPerSecond != null && externalRotationChangesPerSecond >= MAX_EXTERNAL_ROTATIONS_PER_SECOND) {
                        // Something external is changing the screen orientation too quickly, so apps are probably fighting over the screen orientation
                        GenericError error = new GenericError(context, R.string.environment_error_screen_orientation_changed_summary, context.getText(R.string.environment_error_screen_orientation_changed_message));
                        EnvironmentErrorManager.report(context, error);
                    } else {
                        /*
                         * Android P sometimes changes the value of USER_ROTATION to match the current screen orientation.
                         * This can be problematic, since the current screen orientation might be the result of an open app's
                         * orientation preferences. For example, when the launcher is open, the screen orientation is usually portrait.
                         *
                         * That breaks this app, so we'll just have to fight with the OS and change it back.
                         */
                        Log.i(TAG, "Something proposed orientation " + newOrientation + ". Reverting to " + lastProposed + ".");
                        setAndHandleErrors(Settings.System.USER_ROTATION, AndroidSurfaceRotation.fromOrientationClockwise(lastProposed));
                    }
                }
            }
        }
    };

    private final EventHandler systemSettingPermissionChanged = this::validateSystemSettingsPermission;

    /**
     * Raises an {@link samuelpalmer.sensorautorotation.errors.EnvironmentError} if system settings permission is missing.
     * @return Whether the permission is granted
     */
    private boolean validateSystemSettingsPermission() {
        if (!systemSettingPermissionMonitor.hasPermission()) {
            GenericError error = new GenericError(context, R.string.permission_needed_summary, context.getString(R.string.environment_error_system_setting_write_denied_message, context.getText(R.string.app_name_full)));
            EnvironmentErrorManager.report(context, error);
            return false;
        }
        else
            return true;
    }
}
