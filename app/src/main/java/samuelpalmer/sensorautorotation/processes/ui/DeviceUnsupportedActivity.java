package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

//Intentionally not inheriting from custom activity base classes since we don't want any extra behaviour for this activity since it's an error message intended to guide the user away from using the app.
public class DeviceUnsupportedActivity extends AppCompatActivity {
	
	/**
	 * String resource id for explanation text.
	 */
	public static final String EXTRA_EXPLANATION = "explanation";
	public static final String EXTRA_IS_ERROR = "is_error";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_device_unsupported);
		
		int explanationStringResource = getIntent().getIntExtra(EXTRA_EXPLANATION, 0);
		if (explanationStringResource == 0)
			throw new RuntimeException("Explanation not provided");
		
		TextView explanationView = findViewById(R.id.device_unsupported_explanation);
		explanationView.setText(explanationStringResource);
		
		Button uninstallButton = findViewById(R.id.device_unsupported_uninstall);
		uninstallButton.setOnClickListener(v -> Uninstaller.uninstall(DeviceUnsupportedActivity.this));
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		boolean isError = getIntent().getBooleanExtra(EXTRA_IS_ERROR, false);
		if (isError)
			new ApplicationCommand(ApplicationCommand.ACTION_CLEAR_ERROR).send(this);
	}
	
}
