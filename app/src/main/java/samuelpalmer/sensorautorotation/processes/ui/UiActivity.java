package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

public abstract class UiActivity extends AppCompatActivity {
	
	@Override
	protected void onStart() {
		super.onStart();
		
		//Checking this after in onStart() instead of onCreate() in case the user wants to go out and back in again to retry the check in case the problem is intermittent
		int startupErrorString = AppCompatibilityCheck.check(this);
		if (startupErrorString != 0) {
			//Not letting the user go any further since they shouldn't have installed the app
			Log.w(getClass().getSimpleName(), getText(startupErrorString).toString());
			Intent intent = new Intent(this, DeviceUnsupportedActivity.class)
				.putExtra(DeviceUnsupportedActivity.EXTRA_EXPLANATION, startupErrorString)
				.putExtra(DeviceUnsupportedActivity.EXTRA_IS_ERROR, false)
			;
			startActivity(intent);
			
			finish();
			return;
		}
		
		if (!(this instanceof InstructionsActivity)) {
			boolean userInstructed = ApplicationSettings
				.getSharedPreferences(this)
				.getBoolean(ApplicationSettings.KEY_USER_INSTRUCTED, false);
			
			if (!userInstructed) {
				Intent intent = new Intent(this, InstructionsActivity.class)
					.putExtra(InstructionsActivity.EXTRA_FIRST_RUN, true);
				startActivity(intent);
			}
		}
	}
	
}
