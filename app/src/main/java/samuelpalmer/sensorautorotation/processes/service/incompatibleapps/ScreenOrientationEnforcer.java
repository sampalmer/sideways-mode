package samuelpalmer.sensorautorotation.processes.service.incompatibleapps;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import androidx.core.content.ContextCompat;

import samuelpalmer.sensorautorotation.utilities.AlwaysGrantedPermissionMonitor;
import samuelpalmer.sensorautorotation.utilities.AppOpMonitor;
import samuelpalmer.sensorautorotation.utilities.PermissionMonitor;
import samuelpalmer.sensorautorotation.utilities.State;

/**
 * Forces the OS to use the {@link ActivityInfo#SCREEN_ORIENTATION_USER} screen orientation.
 * Requires the {@link android.Manifest.permission#SYSTEM_ALERT_WINDOW} permission.
 */
public class ScreenOrientationEnforcer extends State {

    private static final String TAG = ScreenOrientationEnforcer.class.getSimpleName();

    private final View view;
    private final WindowManager windows;
    private final LayoutParams layout;
    private boolean isEnforced;

    public ScreenOrientationEnforcer(Context context) {
        windows = ContextCompat.getSystemService(context, WindowManager.class);
        view = new View(context);
        layout = generateLayout();
    }

    public static PermissionMonitor makePermissionMonitor(Context context) {
        // Using App Ops instead of Settings.canDrawOverlays() to ensure permission polling is consistent with notifications from the OS.
        // This also makes automated tests of these features more reliable.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return new AppOpMonitor(context, AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW);
        else
            return new AlwaysGrantedPermissionMonitor();
    }

    public static int getWindowType() {
        if (Build.VERSION.SDK_INT >= 26)
            return LayoutParams.TYPE_APPLICATION_OVERLAY;
        else
            // This seems like the most relevant window type for what we're doing.
            // Note that TYPE_TOAST can't be used to bypass the permission requirement since it still needs the permission in MIUI V8
            //noinspection deprecation
            return LayoutParams.TYPE_SYSTEM_OVERLAY;
    }

    @Override
    protected void start() {
        isEnforced = false;
    }

    @Override
    protected void stop() {
        if (isEnforced)
            windows.removeView(view);
    }

    private static LayoutParams generateLayout() {
        LayoutParams layoutParams = new LayoutParams();

        layoutParams.type = getWindowType();

        // Just in case the window type somehow doesn't enforce this
        layoutParams.flags = LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;

        // Prevents breaking apps that detect overlying windows
        // (eg UBank app, or enabling an accessibility service)
        layoutParams.width = 0;
        layoutParams.height = 0;

        // Try to make it completely invisible
        layoutParams.format = PixelFormat.TRANSPARENT;
        layoutParams.alpha = 0f;

        // Match the system's "user orientation" (which this app uses to specify screen orientation)
        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_USER;

        return layoutParams;
    }

    public void setEnforced(boolean newEnforced) {
        verifyStarted();

        if (newEnforced != isEnforced) {
            isEnforced = newEnforced;

            /*
             * This seems to be the quickest way of toggling enforcement. Changing the view visibility is
             * slower, and removing and adding the view is even slower again.
             *
             * There are a few ways of toggling screen orientation enforcement:
             * 1. Fastest: Toggle its {@link LayoutParams#screenOrientation} value. This causes the Android 6 "Screen Overlay Detected" problem even when enforcement is disabled.
             * 2. Medium: Toggle the view's visibility. This doesn't work in Android R, since even "INVISIBLE" or "GONE" windows control orientation.
             * 3. Slowest: Add/remove the view from the window manager.
             *
             * Picking the last option since it's the fastest one that doesn't have undesired side effects.
             */
            if (isEnforced)
                windows.addView(view, layout);
            else
                windows.removeView(view);

            Log.i(TAG, "Set screen orientation enforcement: " + isEnforced);
        }
    }
}
