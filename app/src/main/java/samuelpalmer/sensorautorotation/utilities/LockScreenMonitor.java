package samuelpalmer.sensorautorotation.utilities;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrarAdaptor;

/**
 * Detects when the device is locked.
 */
public class LockScreenMonitor extends EventRegistrarAdaptor {

    private final KeyguardManager keyguardManager;
    private final Context context;

    public LockScreenMonitor(Context context) {
        this.context = context;
        keyguardManager = ContextCompat.getSystemService(this.context, KeyguardManager.class);
    }

    private Boolean isOnLockScreen;
    private boolean monitoring;

    public boolean isOnLockScreen() {
        if (!monitoring)
            return poll(keyguardManager);
        else {
            if (isOnLockScreen == null)
                isOnLockScreen = poll(keyguardManager);
            return isOnLockScreen;
        }
    }

    public static boolean poll(KeyguardManager keyguardManager) {
        return keyguardManager.isKeyguardLocked();
    }

    @Override
    protected void subscribeToAdaptee() {
        for (String action : actions())
            context.registerReceiver(anythingChangedReceiver, new IntentFilter(action));
        monitoring = true;
    }

    @Override
    protected void unsubscribeFromAdaptee() {
        monitoring = false;
        isOnLockScreen = null;
        context.unregisterReceiver(anythingChangedReceiver);
    }

    private Iterable<String> actions() {
        List<String> result = new ArrayList<>(Arrays.asList(
                Intent.ACTION_SCREEN_OFF,
                Intent.ACTION_SCREEN_ON,
                Intent.ACTION_USER_PRESENT
        ));

        if (Build.VERSION.SDK_INT >= 17)
            result.addAll(Arrays.asList(
                    Intent.ACTION_USER_BACKGROUND,
                    Intent.ACTION_USER_FOREGROUND
            ));

        return result;
    }

    private final BroadcastReceiver anythingChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (hasSubscriptions()) {
                boolean newValue = poll(keyguardManager);
                if (isOnLockScreen == null || isOnLockScreen != newValue) {
                    isOnLockScreen = newValue;
                    report();
                }
            }
        }
    };
}
