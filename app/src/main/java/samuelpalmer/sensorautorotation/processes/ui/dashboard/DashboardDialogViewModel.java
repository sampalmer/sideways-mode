package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

/**
 * View-model for {@link DashboardDialogFragment}
 */
@SuppressWarnings("WeakerAccess")
public class DashboardDialogViewModel extends AndroidViewModel {

    private final DashboardSectionLiveData dashboardSection;
    private final PermissionToStartLiveData permissionToStartLiveData;

    public DashboardDialogViewModel(@NonNull Application application) {
        super(application);
        dashboardSection = new DashboardSectionLiveData(application);
        permissionToStartLiveData = new PermissionToStartLiveData(application);
    }

    public LiveData<DashboardSection> getDashboardSection() {
        return dashboardSection;
    }

    public LiveData<Boolean> getPermissionToStart() {
        return permissionToStartLiveData;
    }

}
