package samuelpalmer.sensorautorotation.processes.ui;

import android.app.KeyguardManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;

import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

/**
 * Adds functionality to prompt the user to unlock the device
 */
public class DismissKeyguardFragment extends Fragment {

    private BaseDismissKeyguardImpl impl;

    @Nullable
    private Runnable pendingRunnable;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        impl = Build.VERSION.SDK_INT >= 26 ? new Api26DismissKeyguardFragment() : new OlderApisDismissKeyguardFragment();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        pendingRunnable = null;
        impl = null;
    }

    public void unlockAndRun(Runnable runnable) {
        pendingRunnable = runnable;
        impl.startUnlock();
    }

    private void onUnlockComplete() {
        if (pendingRunnable != null) {
            pendingRunnable.run();
            pendingRunnable = null;
        }
    }

    @RequiresApi(26)
    private class Api26DismissKeyguardFragment extends BaseDismissKeyguardImpl {

        @Override
        public void startUnlock() {
            KeyguardManager keyguardManager = ContextCompat.getSystemService(requireContext(), KeyguardManager.class);
            assert keyguardManager != null;
            keyguardManager.requestDismissKeyguard(requireActivity(), new KeyguardManager.KeyguardDismissCallback() {
                @Override
                public void onDismissSucceeded() {
                    super.onDismissSucceeded();
                    onUnlockComplete();
                }
            });
            // We've prompted the user to unlock the device, so no further work is needed
        }

    }

    private class OlderApisDismissKeyguardFragment extends BaseDismissKeyguardImpl {

        private LockScreenMonitor lockScreenMonitor;
        private boolean isUnlocking = false;

        @Override
        public void startUnlock() {
            if (isUnlocking) {
                // The user probably cancelled the last unlock attempt, so we're still waiting for it to finish.
                // In order to make the keyguard appear again, we need to remove FLAG_DISMISS_KEYGUARD, wait a bit, then re-add it
                cleanUp();
                HandlerCompat.createAsync(Looper.getMainLooper()).post(this::startUnlock);
                return;
            }

            // We cannot show the dialog when locked or the OS won't prompt the user to unlock
            requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

            // We need to keep this activity open until the user finishes with the keyguard.
            // If we close too soon, then the keyguard will disappear and the user will have to start again.
            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

            lockScreenMonitor = new LockScreenMonitor(requireContext());
            lockScreenMonitor.subscribe(lockScreenStatusChanged);
            lockScreenStatusChanged.update();

            isUnlocking = true;
        }

        private final EventHandler lockScreenStatusChanged = new EventHandler() {
            @Override
            public void update() {
                if (isUnlocking) {
                    // We're no longer locked, so we're done
                    if (!lockScreenMonitor.isOnLockScreen()) {
                        // The device has just been unlocked, so we're good to cleanup and proceed
                        cleanUp();
                        onUnlockComplete();
                    }
                }
            }
        };

        private void cleanUp() {
            if (isUnlocking) {
                isUnlocking = false;

                lockScreenMonitor.unsubscribe(lockScreenStatusChanged);
                lockScreenMonitor = null;

                requireActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
            }
        }

    }

    private abstract static class BaseDismissKeyguardImpl {
        public abstract void startUnlock();
    }

}
