package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.CurrentProcess;

public class MessageDialogActivity extends DialogActivity {
	private static final String INTENT_EXTRA_TITLE = "title";
	private static final String INTENT_EXTRA_MESSAGE = "message";
	private static final String TAG = "dialog";

	public static PendingIntent makePendingIntent(CharSequence title, CharSequence message) {
		Intent settingsIntent = ComponentType.create(MainActivity.class).intent();

		ComponentType componentType = ComponentType.create(MessageDialogActivity.class);
		Intent intent = componentType.intent()
				.putExtra(INTENT_EXTRA_MESSAGE, message)
				.putExtra(INTENT_EXTRA_TITLE, title);

		Intent[] intents = {settingsIntent, intent};
		return PendingIntent.getActivities(CurrentProcess.context(), 0, intents, PendingIntent.FLAG_CANCEL_CURRENT);
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
			return new AlertDialog.Builder(getActivity())
					.setMessage(getArguments().getCharSequence(INTENT_EXTRA_MESSAGE))
					.setTitle(getArguments().getCharSequence(INTENT_EXTRA_TITLE))
					.create();
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
