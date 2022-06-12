package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.lifecycle.LiveData;

import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.PermissionMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

class DashboardSectionLiveData extends LiveData<DashboardSection> {

    private final SharedPreferences prefs;

    private final PermissionMonitor permissionMonitor;

    public DashboardSectionLiveData(Context context) {
        permissionMonitor = ScreenOrientationController.makePermissionMonitor(context);
        prefs = ApplicationSettings.getSharedPreferences(context);
    }

    @Override
    protected void onActive() {
        super.onActive();
        permissionMonitor.subscribe(permissionChanged);
        prefs.registerOnSharedPreferenceChangeListener(prefsChanged);
        somethingChanged();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        prefs.unregisterOnSharedPreferenceChangeListener(prefsChanged);
        permissionMonitor.unsubscribe(permissionChanged);
    }

    private final EventHandler permissionChanged = this::somethingChanged;
    private final SharedPreferences.OnSharedPreferenceChangeListener prefsChanged = (sharedPreferences, s) -> somethingChanged();

    private void somethingChanged() {
        if (!permissionMonitor.hasPermission()) {
            setValue(DashboardSection.PERMISSION);
        } else if (needsQuickSetting()) {
            setValue(DashboardSection.QUICK_SETTING);
        } else {
            setValue(DashboardSection.NORMAL);
        }
    }

    private boolean needsQuickSetting() {
        if (Build.VERSION.SDK_INT < 33) {
            // Older OS versions show the service notification out-of-the-box and don't have a way
            // to prompt the user to add the quick setting tile, so no need to suggest the quick
            // setting.
            return false;
        }

        boolean userInformed = prefs.getBoolean(ApplicationSettings.KEY_RUNNING_IN_BACKGROUND_TIP_SHOWN, false);

        // No need to suggest the quick setting if the user already knows how things work
        return !userInformed;
    }

}
