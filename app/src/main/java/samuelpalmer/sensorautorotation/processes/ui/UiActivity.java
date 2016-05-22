package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.ColourGetterCompat;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class UiActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		colourStatusBarAndRecents();
	}

	private void colourStatusBarAndRecents() {
		if (Build.VERSION.SDK_INT >= 21) {
			Window window = getWindow();
			window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
			window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			window.setStatusBarColor(ColourGetterCompat.getColor(this, R.color.primary_700));

			setTaskDescription(new ActivityManager.TaskDescription(null, null, ColourGetterCompat.getColor(this, R.color.primary_500)));
		}
	}

	/**
	 * Ensures the background service is running if necessary in case it's died.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		new ServiceSynchroniser(ApplicationSettings.remote()).updateFromSettings();
	}

}
