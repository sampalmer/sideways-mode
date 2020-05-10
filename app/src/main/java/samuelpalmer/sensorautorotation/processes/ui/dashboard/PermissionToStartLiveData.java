package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Context;

import androidx.lifecycle.LiveData;

import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationController;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventRegistrar;

public class PermissionToStartLiveData extends LiveData<Boolean> {

    private final Context context;
    private final EventRegistrar permissionMonitor;

    public PermissionToStartLiveData(Context context) {
        this.context = context;
        permissionMonitor = ScreenOrientationController.makePermissionMonitor(this.context);
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
        boolean hasPermission = ScreenOrientationController.hasPermissionToStart(context);
        setValue(hasPermission);
    }

}
