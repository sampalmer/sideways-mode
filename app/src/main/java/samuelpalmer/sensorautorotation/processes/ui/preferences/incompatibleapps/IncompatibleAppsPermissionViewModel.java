package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.processes.service.incompatibleapps.activitymonitors.ActivityMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrar;

public class IncompatibleAppsPermissionViewModel extends AndroidViewModel {

    private final MutableLiveData<Boolean> shouldShowTip = new MutableLiveData<>();

    private final ActivityMonitor activityMonitor;
    private final EventRegistrar enforcementPermissionMonitor;

    public LiveData<Boolean > getShouldShowTip() {
        return shouldShowTip;
    }

    public IncompatibleAppsPermissionViewModel(@NonNull Application application) {
        super(application);

        activityMonitor = ActivityMonitor.create(application);
        enforcementPermissionMonitor = ScreenOrientationEnforcer.makePermissionMonitor(application);

        activityMonitor.onPermissionChange().subscribe(onPermissionsChanged);
        enforcementPermissionMonitor.subscribe(onPermissionsChanged);

        updateShouldShowTip(); // Updating the visibility of the tip in onStart() isn't early enough when switching between split and fullscreen on Android 7.1
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        enforcementPermissionMonitor.unsubscribe(onPermissionsChanged);
        activityMonitor.onPermissionChange().unsubscribe(onPermissionsChanged);
    }

    private final EventHandler onPermissionsChanged = this::updateShouldShowTip;

    private void updateShouldShowTip() {
        boolean result = !activityMonitor.hasPermission() || !ScreenOrientationEnforcer.hasPermissionToStart(getApplication());
        shouldShowTip.setValue(result);
    }

}
