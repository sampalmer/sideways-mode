package samuelpalmer.sensorautorotation.processes.ui;

import android.os.Bundle;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public class InstructionsActivity extends FullScreenDialogActivity {
	
	public static final String EXTRA_FIRST_RUN = "first_run";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_instructions);
		
		TextView uninstallingContents = findViewById(R.id.instructions_uninstalling_contents);
		TextView helpContents = findViewById(R.id.instructions_help_contents);
		
		uninstallingContents.setText(
			getResources().getString(
				R.string.instructions_uninstalling_contents,
				getResources().getText(R.string.action_uninstall),
				getResources().getText(R.string.app_name_full)
			)
		);
		
		helpContents.setText(
			getResources().getString(R.string.instructions_help_contents)
		);
	}
	
	@Override
	protected void onFullScreenDialogCloseButtonPressed() {
		completeInstructions();
		super.onFullScreenDialogCloseButtonPressed();
	}
	
	@Override
	public void onBackPressed() {
		completeInstructions();
		super.onBackPressed();
	}
	
	/**
	 * Indicates that the user has finished working through the instructions.
	 */
	private void completeInstructions() {
		if (isFirstRun())
			ApplicationSettings
				.getSharedPreferences(this)
				.edit()
				.putBoolean(ApplicationSettings.KEY_USER_INSTRUCTED, true)
				.apply();
	}
	
	private boolean isFirstRun() {
		return getIntent().getBooleanExtra(EXTRA_FIRST_RUN, false);
	}
	
}
