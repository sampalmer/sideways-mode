package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.app.ActivityManager;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public class DashboardActivity extends DialogActivity implements RunningInBackgroundTipDialogFragment.OnCompletionListener {

    private static final String TAG_DASHBOARD_DIALOG = "dashboard";
    private static final String TAG_RUNNING_IN_BACKGROUND_DIALOG = "runningInBackground";

    public DashboardDialogFragment dashboardDialog;
    private SharedPreferences applicationSettings;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTitle(R.string.app_name_full);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // In the manifest, the activity name is set to the *short* name.
            // Unfortunately, this also changes the task name, which is inconsistent with the other activities.
            // So using this hack to change the task name back to the full name.
            ActivityManager.TaskDescription taskDescription = new ActivityManager.TaskDescription(getString(R.string.app_name_full));
            setTaskDescription(taskDescription);
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        applicationSettings = ApplicationSettings.getSharedPreferences(this);

        if (savedInstanceState == null) {
            dashboardDialog = new DashboardDialogFragment();
            dashboardDialog.show(fragmentManager, TAG_DASHBOARD_DIALOG);
        }
        else
            dashboardDialog = (DashboardDialogFragment) fragmentManager.findFragmentByTag(TAG_DASHBOARD_DIALOG);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dashboardDialog = null;
        applicationSettings = null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (dashboardDialog != null)
            dashboardDialog.setCommandSentListener(this::onCommandSent);
    }

    @Override
    public void onCompleted() {
        applicationSettings
                .edit()
                .putBoolean(ApplicationSettings.KEY_RUNNING_IN_BACKGROUND_TIP_SHOWN, true)
                .apply();

        finish();
    }

    private void onCommandSent(boolean isStartCommand) {
        if (isStartCommand) {
            // No need to show the user how to turn on the app if they've already done so.
            boolean turningOnTipShown = applicationSettings.getBoolean(ApplicationSettings.KEY_TURNING_ON_TIP_SHOWN, false);
            if (!turningOnTipShown)
                applicationSettings
                        .edit()
                        .putBoolean(ApplicationSettings.KEY_TURNING_ON_TIP_SHOWN, true)
                        .apply();
        }

        boolean runningInBackgroundTipShown = applicationSettings.getBoolean(ApplicationSettings.KEY_RUNNING_IN_BACKGROUND_TIP_SHOWN, false);
        if (isStartCommand && !runningInBackgroundTipShown) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.remove(dashboardDialog);
            new RunningInBackgroundTipDialogFragment().show(fragmentTransaction, DashboardActivity.TAG_RUNNING_IN_BACKGROUND_DIALOG);
        } else
            finish();
    }
}
