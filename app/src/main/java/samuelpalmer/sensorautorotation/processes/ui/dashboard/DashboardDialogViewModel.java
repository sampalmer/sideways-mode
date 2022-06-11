package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

/**
 * View-model for {@link DashboardDialogFragment}
 */
public class DashboardDialogViewModel extends AndroidViewModel {

    private final PermissionToStartLiveData permissionToStartLiveData;

    public DashboardDialogViewModel(@NonNull Application application) {
        super(application);
        permissionToStartLiveData = new PermissionToStartLiveData(application);
    }

    public LiveData<Boolean> getPermissionToStart() {
        return permissionToStartLiveData;
    }

}
