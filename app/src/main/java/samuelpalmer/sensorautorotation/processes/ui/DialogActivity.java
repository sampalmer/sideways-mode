package samuelpalmer.sensorautorotation.processes.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.Process;

public class DialogActivity extends UiActivity {
	private static final String INTENT_EXTRA_TITLE = "title";
	private static final String INTENT_EXTRA_MESSAGE = "message";

	public static PendingIntent makePendingIntent(CharSequence title, CharSequence message) {
		Intent settingsIntent = ComponentType.create(MainActivity.class).intent();

		ComponentType componentType = ComponentType.create(DialogActivity.class);
		Intent intent = componentType.intent()
				.putExtra(INTENT_EXTRA_MESSAGE, message)
				.putExtra(INTENT_EXTRA_TITLE, title);

		Intent[] intents = {settingsIntent, intent};
		return PendingIntent.getActivities(Process.context(), 0, intents, PendingIntent.FLAG_CANCEL_CURRENT);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras == null)
			throw new RuntimeException("No extras specified");

		MyDialogFragment.newInstance(extras).show(getFragmentManager(), null);
	}

	public static class MyDialogFragment extends DialogFragment {

		public static MyDialogFragment newInstance(Bundle args) {
			MyDialogFragment frag = new MyDialogFragment();
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
			getActivity().finish();
		}
	}
}
