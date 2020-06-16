package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

/**
 * Locks the activity's screen orientation while the device is on the lock screen.
 */
public class PreventOrientationChangeWhenLockedFragment extends Fragment {

    private LockScreenMonitor lockScreenMonitor;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        lockScreenMonitor = new LockScreenMonitor(requireActivity());
        lockScreenMonitor.subscribe(lockScreenStatusChanged);
        lockScreenStatusChanged.update();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        lockScreenMonitor.unsubscribe(lockScreenStatusChanged);
        lockScreenMonitor = null;
    }

    private final EventHandler lockScreenStatusChanged = new EventHandler() {
        @Override
        public void update() {
            boolean isOnLockScreen = lockScreenMonitor.isOnLockScreen();

            // Unfortunately, we can't use the "locked" screen orientation mode prior to API 18, so those users will just have to miss out
            int newScreenOrientation = isOnLockScreen && Build.VERSION.SDK_INT >= 18 ? ActivityInfo.SCREEN_ORIENTATION_LOCKED : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
            requireActivity().setRequestedOrientation(newScreenOrientation);
        }
    };

}
