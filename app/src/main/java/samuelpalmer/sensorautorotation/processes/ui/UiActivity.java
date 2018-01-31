package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;

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
				FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.TUTORIAL_BEGIN, null);
			}
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// Note that onResume() can apparently be called even if a system window,
		// such as the keyguard, is over the top of the activity. An alternative callback to use
		// is onWindowFocusChanged(), but the problem with that one is it isn't fired for dialog
		// activities, such as the dashboard.
		FirebaseAnalytics.getInstance(this).logEvent(FirebaseAnalytics.Event.APP_OPEN, null);
		
		// Note that we will also end up here when the configuration changes, such as when the screen orientaiton changes.
		// This will also fire each time the user changes activity while the app is open.
		// This could be a bad thing, or maybe it won't matter.
	}
	
}
