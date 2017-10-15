package samuelpalmer.sensorautorotation.processes.ui;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public class InstructionsActivity extends UiActivity {
	
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
	public boolean onCreateOptionsMenu(Menu menu) {
		ActionBar supportActionBar = getSupportActionBar();
		assert supportActionBar != null;
		supportActionBar.setDisplayHomeAsUpEnabled(true);
		
		if (isFirstRun())
			supportActionBar.setHomeAsUpIndicator(R.drawable.close);
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) { // User pressed action bar back/close button
			completeInstructions();
			finish();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
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
		if (isFirstRun()) {
			int appVersionCode;
			try {
				appVersionCode = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
			} catch (PackageManager.NameNotFoundException e) {
				throw new RuntimeException(e);
			}
			
			ApplicationSettings.instance().lastVersionUserInstructedAbout().set(appVersionCode);
		}
	}
	
	private boolean isFirstRun() {
		return getIntent().getBooleanExtra(EXTRA_FIRST_RUN, false);
	}
	
}
