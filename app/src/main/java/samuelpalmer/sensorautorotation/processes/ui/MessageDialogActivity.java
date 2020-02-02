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
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

public class MessageDialogActivity extends DialogActivity {
    private static final String INTENT_EXTRA_TITLE = "title";
    private static final String INTENT_EXTRA_MESSAGE = "message";
    private static final String INTENT_EXTRA_IS_ERROR = "is_error";
    private static final String TAG = "dialog";

    public static PendingIntent makePendingIntent(Context context, CharSequence title, CharSequence message, int requestCode, boolean isError) {
        // Opening the message over the top of the dashboard to give the user context that it's
        // about this app, and since it makes more sense to send the user here than to the settings
        // screen.
        Intent dashboardIntent = new Intent(context, DashboardActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Not passing Intent.FLAG_ACTIVITY_NEW_TASK since the documentation for PendingIntent.getActivities() says to only supply that flag to the first activity in the array.
        Intent messageIntent = new Intent(context, MessageDialogActivity.class)
                .putExtra(INTENT_EXTRA_MESSAGE, message)
                .putExtra(INTENT_EXTRA_TITLE, title)
                .putExtra(INTENT_EXTRA_IS_ERROR, isError);

        Intent[] intents = {dashboardIntent, messageIntent};
        return PendingIntent.getActivities(context, requestCode, intents, PendingIntentIds.PENDING_INTENT_FLAGS);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras == null)
            throw new RuntimeException("No extras specified");

        if (getSupportFragmentManager().findFragmentByTag(TAG) == null)
            MessageDialogFragment.newInstance(extras).show(getSupportFragmentManager(), TAG);
    }

    @SuppressWarnings("WeakerAccess") // Because fragments must be public
    public static class MessageDialogFragment extends AppCompatDialogFragment {

        private static MessageDialogFragment newInstance(Bundle args) {
            MessageDialogFragment frag = new MessageDialogFragment();
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (getArguments() == null)
                throw new NullPointerException("Arguments were not provided");

            Dialog result = new AlertDialog.Builder(requireActivity())
                    .setMessage(getArguments().getCharSequence(INTENT_EXTRA_MESSAGE))
                    .setTitle(getArguments().getCharSequence(INTENT_EXTRA_TITLE))
                    .create();
            
            if (getArguments().getBoolean(INTENT_EXTRA_IS_ERROR))
                new ApplicationCommand(ApplicationCommand.ACTION_CLEAR_ERROR).send(getActivity());
            
            return result;
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
