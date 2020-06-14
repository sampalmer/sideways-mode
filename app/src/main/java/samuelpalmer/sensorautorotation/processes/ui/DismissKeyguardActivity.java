package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

public class DismissKeyguardActivity extends Activity {

    /**
     * Launches the activity for the given intent, unlocking the keyguard if necessary.
     * @return Whether the keyguard will be unlocked
     */
    public static boolean dismissAndLaunch(Activity currentActivity, Intent targetIntent) {
        KeyguardManager keyguardManager = ContextCompat.getSystemService(currentActivity, KeyguardManager.class);
        assert keyguardManager != null;
        boolean isLocked = LockScreenMonitor.poll(keyguardManager);

        if (isLocked) {
            Intent unlockIntent = new Intent(currentActivity, DismissKeyguardActivity.class);
            currentActivity.startActivities(new Intent[]{targetIntent, unlockIntent});
        } else
            currentActivity.startActivity(targetIntent);

        return isLocked;
    }

    private BaseDismissKeyguardImpl impl;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Not using fragments here because they require use of the support library and its themes
        impl = Build.VERSION.SDK_INT >= 26 ? new Api26DismissKeyguardFragment() : new OlderApisDismissKeyguardFragment();
        impl.onActivityCreated();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        impl.onActivityDestroyed();
        impl = null;
    }

    @RequiresApi(26)
    private class Api26DismissKeyguardFragment extends BaseDismissKeyguardImpl {

        @Override
        public void onActivityCreated() {
            KeyguardManager keyguardManager = ContextCompat.getSystemService(DismissKeyguardActivity.this, KeyguardManager.class);
            assert keyguardManager != null;
            keyguardManager.requestDismissKeyguard(DismissKeyguardActivity.this, null);

            // We've prompted the user to unlock the device, so no further work is needed
            finish();
        }

    }

    private class OlderApisDismissKeyguardFragment extends BaseDismissKeyguardImpl {

        private LockScreenMonitor lockScreenMonitor;

        @Override
        public void onActivityCreated() {
            // We need to keep this activity open until the user finishes with the keyguard.
            // If we close too soon, then the keyguard will disappear and the user will have to start again.
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            Log.i(DismissKeyguardActivity.class.getSimpleName(), "Activity created");

            lockScreenMonitor = new LockScreenMonitor(DismissKeyguardActivity.this);
            lockScreenMonitor.subscribe(lockScreenStatusChanged);
            lockScreenStatusChanged.update();
        }

        @Override
        public void onActivityDestroyed() {
            lockScreenMonitor.unsubscribe(lockScreenStatusChanged);
            lockScreenMonitor = null;
        }

        private final EventHandler lockScreenStatusChanged = new EventHandler() {
            @Override
            public void update() {
                // We're no longer locked, so we're done
                if (!lockScreenMonitor.isOnLockScreen())
                    finish();
            }
        };

    }

    private abstract static class BaseDismissKeyguardImpl {

        public void onActivityCreated() {}
        public void onActivityDestroyed() {}

    }

}
