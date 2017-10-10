package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.dashboard.DashboardActivity;

public class InstructionsActivity extends UiActivity {
	
	public static final String EXTRA_FIRST_RUN = "first_run";
	public static final String EXTRA_TUTORIAL_COMPLETE = "tutorial_complete";
	
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
			getResources().getString(
				R.string.instructions_help_contents,
				getResources().getText(R.string.action_change_side)
			)
		);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.instructions, menu);
		
		boolean isFirstRun = isFirstRun();
		if (isFirstRun) {
			ActionBar supportActionBar = getSupportActionBar();
			assert supportActionBar != null;
			supportActionBar.setDisplayHomeAsUpEnabled(false); //Remove "back" button from action bar
		}
		
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.skip) {
			completeInstructions();
			return true;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * Indicates that the user has finished working through the instructions.
	 */
	private void completeInstructions() {
		if (isFirstRun()) {
			Intent intent = new Intent(CurrentProcess.context(), DashboardActivity.class)
				.putExtra(EXTRA_TUTORIAL_COMPLETE, true);
			
			startActivity(intent);
		}
		
		finish();
	}
	
	private boolean isFirstRun() {
		return getIntent().getBooleanExtra(EXTRA_FIRST_RUN, false);
	}
	
}
