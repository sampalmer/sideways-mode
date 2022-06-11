package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Context;

import androidx.lifecycle.LiveData;

import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.utilities.PermissionMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

/**
 * Adapts {@link PermissionMonitor} to {@link LiveData}
 */
public class PermissionToStartLiveData extends LiveData<Boolean> {

    private final PermissionMonitor permissionMonitor;

    public PermissionToStartLiveData(Context context) {
        permissionMonitor = ScreenOrientationController.makePermissionMonitor(context);
    }

    @Override
    protected void onActive() {
        super.onActive();
        permissionMonitor.subscribe(permissionChanged);
        checkPermission();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        permissionMonitor.unsubscribe(permissionChanged);
    }

    private final EventHandler permissionChanged = this::checkPermission;

    private void checkPermission() {
        boolean hasPermission = permissionMonitor.hasPermission();
        setValue(hasPermission);
    }

}
