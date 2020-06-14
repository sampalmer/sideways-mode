package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.KeyguardManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

/**
 * Allows the host activity to be started on the lock screen, but also prevents it from showing on
 * the lock screen if the device is locked while it is already open.
 */
public class AllowLaunchOnLockScreenFragment extends Fragment {

    private KeyguardManager keyguardManager;
    private boolean shouldShowOnLockScreen = false;
    private LockScreenMonitor lockScreenMonitor;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        keyguardManager = ContextCompat.getSystemService(requireContext(), KeyguardManager.class);
        lockScreenMonitor = new LockScreenMonitor(requireContext());
        lockScreenMonitor.subscribe(lockScreenStatusChanged);

        // The activity has just been created, so we'll only allow it on the lock screen if it was launched from the lock screen
        shouldShowOnLockScreen = LockScreenMonitor.poll(keyguardManager);
        updateCanShowOnLockScreen();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        lockScreenMonitor.unsubscribe(lockScreenStatusChanged);
        lockScreenMonitor = null;
        keyguardManager = null;
    }

    public void onNewIntent() {
        // The activity has just been re-launched, so we'll only allow it on the lock screen if it was launched from the lock screen
        shouldShowOnLockScreen = LockScreenMonitor.poll(keyguardManager);
        updateCanShowOnLockScreen();
    }

    private final EventHandler lockScreenStatusChanged = new EventHandler() {
        @Override
        public void update() {
            if (!lockScreenMonitor.isOnLockScreen()) {
                // The device was just unlocked, so we'll prevent it from showing on the lock screen
                // This is needed in the following scenario:
                // 1. Run Android 10 emulator
                // 2. Open dashboard from lock screen
                // 3. Open app settings
                // 4. Unlock device when prompted
                // 5. Press back button to return from settings screen to dashboard
                // 6. Re-lock device
                // 7. Turn screen on
                shouldShowOnLockScreen = false;
                updateCanShowOnLockScreen();
            }
        }
    };

    private void updateCanShowOnLockScreen() {
        // Allowing use on lock screen in case the user started the dashboard from the quick setting while locked
        // Not allowing on lock screen if the user isn't on the lock screen so that if the user locks the device while the dashboard is open then the dashboard won't sit on top of the lock screen when the screen is next turned on

        if (Build.VERSION.SDK_INT >= 27)
            requireActivity().setShowWhenLocked(shouldShowOnLockScreen);
        else {
            Window window = requireActivity().getWindow();

            if (shouldShowOnLockScreen)
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            else
                window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
    }
}
