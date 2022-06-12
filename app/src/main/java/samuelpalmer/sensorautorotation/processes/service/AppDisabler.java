package samuelpalmer.sensorautorotation.processes.service;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.content.ContextCompat;

import java.util.Objects;

import samuelpalmer.sensorautorotation.processes.service.api.ApplicationCommandReceiver;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.State;

/**
 * Turns off the app when certain events occur, such as when the device is shutdown.
 */
public class AppDisabler extends State {

    private final KeyguardManager keyguardManager;
    private final Context context;
    private final SharedPreferences prefs;

    private boolean turnOffWhenLocked;
    private boolean isOnLockScreen;

    public AppDisabler(Context context) {
        this.context = context;
        keyguardManager = Objects.requireNonNull(ContextCompat.getSystemService(context, KeyguardManager.class));
        prefs = ServiceSettings.getSharedPreferences(context);
    }
    
    @Override
    protected void start() {
        isOnLockScreen = false;

        prefs.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
        syncPrefs();

        context.registerReceiver(shutdownReceiver, new IntentFilter(Intent.ACTION_SHUTDOWN));
        context.registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_SCREEN_ON));
        context.registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));
        
        if (Build.VERSION.SDK_INT >= 17) {
            context.registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_USER_BACKGROUND));
            context.registerReceiver(screenChangedReceiver, new IntentFilter(Intent.ACTION_USER_FOREGROUND));
        }
        
        context.registerReceiver(unlockReceiver, new IntentFilter(Intent.ACTION_USER_PRESENT));

        // We might be starting the app from the lock screen, so better keep track of that
        if (turnOffWhenLocked)
            isOnLockScreen = LockScreenMonitor.poll(keyguardManager);
    }
    
    @Override
    protected void stop() {
        context.unregisterReceiver(unlockReceiver);
        context.unregisterReceiver(screenChangedReceiver);
        context.unregisterReceiver(shutdownReceiver);

        prefs.unregisterOnSharedPreferenceChangeListener(preferenceChangeListener);
    }

    private final SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = (sharedPreferences, key) -> {
        if (ServiceSettings.KEY_TURN_OFF_WHEN_LOCKED.equals(key))
            syncPrefs();
    };

    private void syncPrefs() {
        turnOffWhenLocked = prefs.getBoolean(ServiceSettings.KEY_TURN_OFF_WHEN_LOCKED, true);

        // If we're going to keep the app running when the device is locked, then we'd better reset the lock screen state
        // so, if the setting is toggled, we aren't out of sync.
        if (!turnOffWhenLocked)
            isOnLockScreen = false;
    }

    private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            // We need to turn off the app when the device shuts down so that:
            // 1. The original value of the system "auto rotate" toggle isn't lost
            // 2. The device's USER_ROTATION value is back to normal when the device next starts up
            if (isStarted())
                ApplicationCommandReceiver.turnOffApp(context, "Device is shutting down");
        }
        
    };
    
    private final BroadcastReceiver screenChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (isStarted() && turnOffWhenLocked) {
                /*
                Don't want to turn the app off when the screen turns off because the screen might
                have turned off due to inactivity while the user is actually still using the device.

                Not subscribing to LockScreenMonitor because it doesn't give us enough control to:
                1. Detect that the screen was locked if the user turns on the screen when the OS lock screen is configured to "none"
                2. Detect that the screen was locked if the user unlocks the phone by fingerprint
                 */
                isOnLockScreen = LockScreenMonitor.poll(keyguardManager);
                if (isOnLockScreen)
                    ApplicationCommandReceiver.turnOffApp(context, "Device is locked");
            }
        }
    };
    
    private final BroadcastReceiver unlockReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Note that we don't get here on API 15 when the screen lock is set to "None". Not much we can do about that.

            /*
             Only proceeding if the user wasn't on the lock screen, because if they were, then:
             1. We would have already turned off the ap, and;
             2. The user might have turned *on* the app from the lock screen (eg by using the persistent notification or quick setting)
             */
            if (isStarted() && turnOffWhenLocked && !isOnLockScreen) {
                /*
                We receive this broadcast after the device gets unlocked.
                This implies that the device was locked, so the app shouldn't be on.
                We need this check because Android doesn't broadcast when the device gets locked,
                and our polling approach might sometimes be too slow to detect the lock screen.
                For example, fingerprint unlock on my Motorola Moto G5 Plus unlocks the device
                so quickly that you don't see the lock screen.
                */
                ApplicationCommandReceiver.turnOffApp(context, "Device was locked");
            }
        }
    };
    
}
