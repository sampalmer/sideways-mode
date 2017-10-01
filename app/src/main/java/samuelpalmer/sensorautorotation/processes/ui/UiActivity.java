package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.processes.ui.instructions.InstructionsActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

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
			Settings.Setting<Integer> lastVersionUserInstructedAbout = ApplicationSettings.instance().lastVersionUserInstructedAbout();
			if (!lastVersionUserInstructedAbout.hasValue()) {
				boolean tutorialComplete = getIntent().getBooleanExtra(InstructionsActivity.EXTRA_TUTORIAL_COMPLETE, false);
				
				if (!tutorialComplete) {
					Intent intent = new Intent(CurrentProcess.context(), InstructionsActivity.class)
						.putExtra(InstructionsActivity.EXTRA_FIRST_RUN, true);
					startActivity(intent);
					
					finish();
				} else {
					int result;
					try {
						result = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
					} catch (PackageManager.NameNotFoundException e) {
						throw new RuntimeException(e);
					}
					
					lastVersionUserInstructedAbout.set(result);
				}
			}
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
