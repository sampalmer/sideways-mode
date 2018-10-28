package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;

public class DashboardActivity extends DialogActivity {

    public static final String TAG_DASHBOARD_DIALOG = "dashboard";
    public static final String TAG_RUNNING_IN_BACKGROUND_DIALOG = "runningInBackground";

    public DashboardDialogFragment dashboardDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();

        dashboardDialog = (DashboardDialogFragment) fragmentManager.findFragmentByTag(TAG_DASHBOARD_DIALOG);
        if (dashboardDialog == null) {
            dashboardDialog = new DashboardDialogFragment();
            dashboardDialog.show(fragmentManager, TAG_DASHBOARD_DIALOG);
        }
    }
}
