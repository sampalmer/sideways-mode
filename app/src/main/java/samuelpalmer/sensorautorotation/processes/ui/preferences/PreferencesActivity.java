package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

/**
 * UI proxy to settings stored in background process.
 */
public class PreferencesActivity extends UiActivity {

	private Settings.Setting<String> lastError;
	private Snackbar snackbar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		lastError = EnvironmentError.lastError();
	}

	@Override
	protected void onStart() {
		super.onStart();

		lastError.subscribe(lastErrorChanged);
		lastErrorChanged.update();
	}

	@Override
	protected void onStop() {
		super.onStop();

		lastError.unsubscribe(lastErrorChanged);
	}

	private final EventHandler lastErrorChanged = new EventHandler() {
		@Override
		public void update() {
			String lastErrorMessage = lastError.tryGet();

			if (snackbar == null) {
				//http://stackoverflow.com/a/5069354/238753
				View contentView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
				snackbar = Snackbar.make(contentView, "", Snackbar.LENGTH_INDEFINITE);
				//TODO: Get the snackbar to show the full message rather than truncating it.
				//TODO: Prevent the snackbar from blocking UI elements underneath it.
			}

			if (lastErrorMessage == null)
				snackbar.dismiss();
			else {
				snackbar.setText(lastErrorMessage);
				snackbar.show();
			}
		}
	};

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		//PreferenceFragment calls this for its underlying preference storage
		if (name.equals(ApplicationSettings.sharedPreferencesName()))
			return ApplicationSettings.remote().sharedPreferences;
		else
			return super.getSharedPreferences(name, mode);
	}

}
