package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import samuelpalmer.sensorautorotation.processes.service.PersistentNotificationSyncReceiver;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

public class DashboardActivity extends AppCompatActivity implements RunningInBackgroundTipDialogFragment.OnCompletionListener {

    private static final String TAG_DASHBOARD_DIALOG = "dashboard";
    private static final String TAG_RUNNING_IN_BACKGROUND_DIALOG = "runningInBackground";

    private DashboardDialogFragment dashboardDialog;
    private SharedPreferences applicationSettings;
    private LockScreenMonitor lockScreenMonitor;
    private boolean isOnLockScreen;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Allowing use on lock screen in case the user started the dashboard from the quick setting while locked
        if (Build.VERSION.SDK_INT >= 27)
            setShowWhenLocked(true);
        else
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);

        FragmentManager fragmentManager = getSupportFragmentManager();
        applicationSettings = ApplicationSettings.getSharedPreferences(this);

        if (savedInstanceState == null) {
            dashboardDialog = new DashboardDialogFragment();
            dashboardDialog.show(fragmentManager, TAG_DASHBOARD_DIALOG);
        }
        else
            dashboardDialog = (DashboardDialogFragment) fragmentManager.findFragmentByTag(TAG_DASHBOARD_DIALOG);

        lockScreenMonitor = new LockScreenMonitor(this);
        lockScreenMonitor.subscribe(lockScreenStatusChanged);
        isOnLockScreen = lockScreenMonitor.isOnLockScreen();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        lockScreenMonitor.unsubscribe(lockScreenStatusChanged);
        lockScreenMonitor = null;

        dashboardDialog = null;
        applicationSettings = null;
    }

    private final EventHandler lockScreenStatusChanged = new EventHandler() {
        @Override
        public void update() {
            boolean wasOnLockScreen = isOnLockScreen;
            isOnLockScreen = lockScreenMonitor.isOnLockScreen();

            // Closing the activity if the user locks their device so it won't display over the lock screen when they next turn the screen on
            if (!wasOnLockScreen && isOnLockScreen)
                finish();
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        // Prevents the error: "Activity {samuelpalmer.sensorautorotation/samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity} did not call finish() prior to onResume() completing"
        // Ideally we would be using a dialog-themed activity instead of an empty activity with nested dialog, but that doesn't work with TapTargetView
        if (Build.VERSION.SDK_INT >= 23)
            setVisible(true);

        if (dashboardDialog != null)
            dashboardDialog.setCommandSentListener(this::onCommandSent);

        // The persistent notification could have been removed, for example by blocking the
        // app's notifications or force-stopping the app. So we'll re-post it here just in-case.
        sendBroadcast(new Intent(this, PersistentNotificationSyncReceiver.class));
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
