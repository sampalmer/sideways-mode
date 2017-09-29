package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.StringResource;

public abstract class UiActivity extends AppCompatActivity {
	
	@Override
	protected void onStart() {
		super.onStart();
		
		//Checking this after in onStart() instead of onCreate() in case the user wants to go out and back in again to retry the check in case the problem is intermittent
		int startupErrorString = AppCompatibilityCheck.check();
		if (startupErrorString != 0) {
			//Not letting the user go any further since they shouldn't have installed the app
			Log.w(MainActivity.class.getSimpleName(), StringResource.get(startupErrorString).toString());
			startActivity(new Intent(this, DeviceUnsupportedActivity.class).putExtra(DeviceUnsupportedActivity.EXTRA_EXPLANATION, startupErrorString));
			
			finish();
		}
	}
	
	/**
	 * Ensures the background service is running if necessary in case it's died.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		ServiceSynchroniser.updateFromSettings();
	}

}
