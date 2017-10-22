package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

public class MessageDialogActivity extends DialogActivity {
	private static final String INTENT_EXTRA_TITLE = "title";
	private static final String INTENT_EXTRA_MESSAGE = "message";
	private static final String INTENT_EXTRA_IS_ERROR = "is_error";
	private static final String TAG = "dialog";

	public static PendingIntent makePendingIntent(CharSequence title, CharSequence message, int requestCode, boolean isError) {
		Intent mainActivityIntent = new Intent(CurrentProcess.context(), DashboardActivity.class);
		
		Intent intent = new Intent(CurrentProcess.context(), MessageDialogActivity.class)
				.putExtra(INTENT_EXTRA_MESSAGE, message)
				.putExtra(INTENT_EXTRA_TITLE, title)
				.putExtra(INTENT_EXTRA_IS_ERROR, isError);

		Intent[] intents = {mainActivityIntent, intent};
		return PendingIntent.getActivities(CurrentProcess.context(), requestCode, intents, PendingIntentIds.PENDING_INTENT_FLAGS);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras == null)
			throw new RuntimeException("No extras specified");

		if (getFragmentManager().findFragmentByTag(TAG) == null)
			MessageDialogFragment.newInstance(extras).show(getFragmentManager(), TAG);
	}

	public static class MessageDialogFragment extends DialogFragment {

		public static MessageDialogFragment newInstance(Bundle args) {
			MessageDialogFragment frag = new MessageDialogFragment();
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Dialog result = new AlertDialog.Builder(getActivity())
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

			//For some reason the activity is null when the screen orientation changes
			if (activity != null)
				activity.finish();
		}
	}
}
