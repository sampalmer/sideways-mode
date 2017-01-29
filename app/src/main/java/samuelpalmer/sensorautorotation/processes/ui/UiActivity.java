package samuelpalmer.sensorautorotation.processes.ui;

import android.support.v7.app.AppCompatActivity;

import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public abstract class UiActivity extends AppCompatActivity {

	/**
	 * Ensures the background service is running if necessary in case it's died.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		new ServiceSynchroniser(ApplicationSettings.remote()).updateFromSettings();
	}

}
