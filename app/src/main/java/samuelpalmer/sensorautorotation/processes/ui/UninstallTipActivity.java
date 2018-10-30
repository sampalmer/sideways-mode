package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public class UninstallTipActivity extends DialogActivity {
    private static final String TAG = "dialog";

    public static PendingIntent makePendingIntent(Context context, int requestCode) {
        Intent intent = new Intent(context, UninstallTipActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(context, requestCode, intent, PendingIntentIds.PENDING_INTENT_FLAGS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentByTag(TAG) == null)
            new UninstallTipFragment().show(getSupportFragmentManager(), TAG);
    }

    public static class UninstallTipFragment extends DialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            @SuppressWarnings("ConstantConditions")
            Dialog result = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.tip_uninstalling_title, getText(R.string.app_name_full)))
                    .setMessage(getString(R.string.tip_uninstalling_description, getText(R.string.app_name_full)))
                    .setPositiveButton(getText(R.string.action_got_it), (dialog, which) -> complete())
                    .create();

            return result;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            super.onCancel(dialog);
            complete();
        }

        private void complete() {
            ApplicationSettings.getSharedPreferences(getContext())
                    .edit()
                    .putBoolean(ApplicationSettings.KEY_UNINSTALLING_TIP_SHOWN, true)
                    .apply();

            @SuppressWarnings("ConstantConditions")
            NotificationManager notificationManager = ContextCompat.getSystemService(getContext(), NotificationManager.class);

            assert notificationManager != null;
            notificationManager.cancel(NotificationIds.UNINSTALL);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            Activity activity = getActivity();

            //For some reason the activity is null when the screen orientation changes
            if (activity != null)
                activity.finish();
        }
    }
}
