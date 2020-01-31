package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public class RemovingIncompatibleAppsTipActivity extends DialogActivity {
    private static final String TAG = "dialog";

    public static PendingIntent makePendingIntent(Context context, int requestCode) {
        Intent intent = new Intent(context, RemovingIncompatibleAppsTipActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        return PendingIntent.getActivity(context, requestCode, intent, PendingIntentIds.PENDING_INTENT_FLAGS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getSupportFragmentManager().findFragmentByTag(TAG) == null)
            new RemovingIncompatibleAppsTipFragment().show(getSupportFragmentManager(), TAG);
    }

    public static class RemovingIncompatibleAppsTipFragment extends AppCompatDialogFragment {

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            @SuppressWarnings("ConstantConditions")
            Dialog result = new AlertDialog.Builder(getActivity())
                    .setTitle(getString(R.string.tip_removing_incompatible_apps_title))
                    .setMessage(getString(R.string.tip_removing_incompatible_apps_description, getText(R.string.preference_incompatible_apps_title)))
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
                    .putBoolean(ApplicationSettings.KEY_REMOVE_INCOMPATIBLE_APPS_TIP_SHOWN, true)
                    .apply();
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            Activity activity = getActivity();

            // For some reason the activity is null when the screen orientation changes
            if (activity != null)
                activity.finish();
        }
    }
}
