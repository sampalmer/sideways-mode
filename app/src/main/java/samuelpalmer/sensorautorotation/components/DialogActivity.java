package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DialogActivity extends Activity {
	public static final String MESSAGE_INTENT_EXTRA_NAME = "message";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle extras = getIntent().getExtras();
		if (extras == null)
			throw new RuntimeException("No extras specified");

		String message = extras.getString(MESSAGE_INTENT_EXTRA_NAME);

		MyDialogFragment.newInstance(message).show(getFragmentManager(), null);
	}

	public static class MyDialogFragment extends DialogFragment {

		public static MyDialogFragment newInstance(String message) {
			MyDialogFragment frag = new MyDialogFragment();
			Bundle args = new Bundle();
			args.putString("message", message);
			frag.setArguments(args);
			return frag;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			return new AlertDialog.Builder(getActivity())
					.setMessage(getArguments().getString("message"))
					.create();
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			getActivity().finish();
		}
	}
}
