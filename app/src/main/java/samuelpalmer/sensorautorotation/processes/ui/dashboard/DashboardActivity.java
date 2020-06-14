package samuelpalmer.sensorautorotation.processes.ui.dashboard;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import samuelpalmer.sensorautorotation.processes.service.PersistentNotificationSyncReceiver;
import samuelpalmer.sensorautorotation.processes.ui.DismissKeyguardFragment;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public class DashboardActivity extends AppCompatActivity implements RunningInBackgroundTipDialogFragment.OnCompletionListener {

    private static final String TAG_DASHBOARD_DIALOG = "dashboard";
    private static final String TAG_ALLOW_ON_LOCK_SCREEN = "allowOnLockScreen";
    private static final String TAG_DISMISS_KEYGUARD = "dismissKeyguard";
    private static final String TAG_RUNNING_IN_BACKGROUND_DIALOG = "runningInBackground";

    private SharedPreferences applicationSettings;
    private DashboardDialogFragment dashboardDialog;
    private AllowLaunchOnLockScreenFragment allowOnLockScreen;
    private DismissKeyguardFragment dismissKeyguardFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FragmentManager fragmentManager = getSupportFragmentManager();
        applicationSettings = ApplicationSettings.getSharedPreferences(this);

        if (savedInstanceState == null) {
            dashboardDialog = new DashboardDialogFragment();
            dashboardDialog.show(fragmentManager, TAG_DASHBOARD_DIALOG);

            allowOnLockScreen = new AllowLaunchOnLockScreenFragment();
            dismissKeyguardFragment = new DismissKeyguardFragment();

            fragmentManager
                    .beginTransaction()
                    .add(allowOnLockScreen, TAG_ALLOW_ON_LOCK_SCREEN)
                    .add(dismissKeyguardFragment, TAG_DISMISS_KEYGUARD)
                    .commitNow();
        }
        else {
            dashboardDialog = (DashboardDialogFragment) fragmentManager.findFragmentByTag(TAG_DASHBOARD_DIALOG);
            allowOnLockScreen = (AllowLaunchOnLockScreenFragment) fragmentManager.findFragmentByTag(TAG_ALLOW_ON_LOCK_SCREEN);
            dismissKeyguardFragment = (DismissKeyguardFragment) fragmentManager.findFragmentByTag(TAG_DISMISS_KEYGUARD);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        allowOnLockScreen.onNewIntent();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        dismissKeyguardFragment = null;
        allowOnLockScreen = null;
        dashboardDialog = null;
        applicationSettings = null;
    }

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

    /**
     * Runs the given runnable, prompting the user to unlock the device if needed
     */
    public void unlockAndRun(Runnable runnable) {
        // We might be on the lock screen if the user launched the dashboard from the quick setting tile,
        // so we'll make sure the device is unlocked
        dismissKeyguardFragment.unlockAndRun(runnable);
    }

}
