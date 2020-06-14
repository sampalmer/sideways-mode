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

/**
 * Allows the host activity to be started on the lock screen, but also prevents it from showing on
 * the lock screen if the device is locked while it is already open.
 */
public class AllowLaunchOnLockScreenFragment extends Fragment {

    private KeyguardManager keyguardManager;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        keyguardManager = ContextCompat.getSystemService(requireContext(), KeyguardManager.class);

        // The activity has just been created, so we'll check whether it was from the lock screen
        updateCanShowOnLockScreen();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        keyguardManager = null;
    }

    public void onNewIntent() {
        // The activity has just been re-launched, so we'll re-check whether it was from the lock screen
        updateCanShowOnLockScreen();
    }

    private void updateCanShowOnLockScreen() {
        // Allowing use on lock screen in case the user started the dashboard from the quick setting while locked
        // Not allowing on lock screen if the user isn't on the lock screen so that if the user locks the device while the dashboard is open then the dashboard won't sit on top of the lock screen when the screen is next turned on
        boolean isOnLockScreen = LockScreenMonitor.poll(keyguardManager);

        if (Build.VERSION.SDK_INT >= 27)
            requireActivity().setShowWhenLocked(isOnLockScreen);
        else {
            Window window = requireActivity().getWindow();

            if (isOnLockScreen)
                window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            else
                window.clearFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        }
    }
}
