package samuelpalmer.sensorautorotation.utilities;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import androidx.annotation.NonNull;

import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

/**
 * Watches for when the OS dark theme is toggled on or off
 */
public final class DarkThemeMonitor extends EventRegistrarAdaptor {

    private final Context context;

    private Boolean isDarkTheme;

    public DarkThemeMonitor(Context context) {
        this.context = context;
    }

    @Override
    protected void subscribeToAdaptee() {
        isDarkTheme = poll(context);
        context.registerComponentCallbacks(componentCallbacks);
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        context.unregisterComponentCallbacks(componentCallbacks);

        // We're no longer watching, so our cached value should be treated as out-of-date
        isDarkTheme = null;
    }

    private final ComponentCallbacks componentCallbacks = new ComponentCallbacks() {

        @Override
        public void onConfigurationChanged(@NonNull Configuration newConfig) {
            if (hasSubscriptions()) {
                boolean newValue = poll(context);
                if (isDarkTheme == null || isDarkTheme != newValue) {
                    isDarkTheme = newValue;
                    report();
                }
            }
        }

        @Override
        public void onLowMemory() {}

    };

    public boolean isDarkTheme() {
        return isDarkTheme == null ? poll(context) : isDarkTheme;
    }

    public static boolean poll(Context context) {
        if (Build.VERSION.SDK_INT >= 30)
            return context.getResources().getConfiguration().isNightModeActive();
        else {
            int uiMode = context.getResources().getConfiguration().uiMode;
            int nightMode = uiMode & Configuration.UI_MODE_NIGHT_MASK;
            return nightMode == Configuration.UI_MODE_NIGHT_YES;
        }
    }

}
