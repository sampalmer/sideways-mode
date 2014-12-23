package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;

public class ErrorDialogActivity extends Activity {

	public static final String MESSAGE_INTENT_EXTRA_NAME = "message";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_error_dialog);

		Bundle extras = getIntent().getExtras();
		if (extras == null)
			throw new RuntimeException("No extras specified");

		String message = extras.getString(MESSAGE_INTENT_EXTRA_NAME);
		TextView text = (TextView) findViewById(R.id.dialog_error_message);
		text.setText(message);
	}

}
