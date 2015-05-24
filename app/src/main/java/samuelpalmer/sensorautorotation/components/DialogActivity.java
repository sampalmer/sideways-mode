package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class DialogActivity extends Activity {
	public static final String INTENT_EXTRA_TITLE = "title";
	public static final String INTENT_EXTRA_MESSAGE = "message";

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
					.setMessage(getArguments().getString(INTENT_EXTRA_MESSAGE))
					.setTitle(getArguments().getCharSequence(INTENT_EXTRA_TITLE))
					.create();
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			getActivity().finish();
		}
	}
}
