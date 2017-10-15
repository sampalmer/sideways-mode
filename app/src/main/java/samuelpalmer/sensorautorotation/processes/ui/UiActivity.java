package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public abstract class UiActivity extends AppCompatActivity {
	
	@Override
	protected void onStart() {
		super.onStart();
		
		//Checking this after in onStart() instead of onCreate() in case the user wants to go out and back in again to retry the check in case the problem is intermittent
		int startupErrorString = AppCompatibilityCheck.check();
		if (startupErrorString != 0) {
			//Not letting the user go any further since they shouldn't have installed the app
			Log.w(getClass().getSimpleName(), CurrentProcess.context().getText(startupErrorString).toString());
			startActivity(new Intent(this, DeviceUnsupportedActivity.class).putExtra(DeviceUnsupportedActivity.EXTRA_EXPLANATION, startupErrorString));
			
			finish();
			return;
		}
		
		if (!(this instanceof InstructionsActivity)) {
			boolean userInstructed = ApplicationSettings.instance().lastVersionUserInstructedAbout().hasValue();
			if (!userInstructed) {
				Intent intent = new Intent(CurrentProcess.context(), InstructionsActivity.class)
					.putExtra(InstructionsActivity.EXTRA_FIRST_RUN, true);
				startActivity(intent);
			}
		}
	}
	
	/**
	 * Ensures the background service is running, just in case it has died.
	 * Also ensures the service process has the latest copy of the user preferences, just in case it somehow got out-of-sync.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		new ApplicationCommand(ApplicationCommand.ACTION_SYNC_SERVICE).send(this);
	}
	
}
