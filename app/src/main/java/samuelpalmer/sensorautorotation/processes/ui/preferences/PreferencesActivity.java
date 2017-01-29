package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

/**
 * UI proxy to settings stored in background process.
 */
public class PreferencesActivity extends UiActivity {

	private Snackbar snackbar;

	@Override
	protected void onStart() {
		super.onStart();

		EnvironmentError.onChange.subscribe(lastErrorChanged);
		lastErrorChanged.update();
	}

	@Override
	protected void onStop() {
		super.onStop();

		EnvironmentError.onChange.unsubscribe(lastErrorChanged);
	}

	private final EventHandler lastErrorChanged = new EventHandler() {
		@Override
		public void update() {
			final EnvironmentError lastError = EnvironmentError.lastError();

			if (snackbar == null) {
				//http://stackoverflow.com/a/5069354/238753
				View contentView = ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
				snackbar = Snackbar.make(contentView, "", Snackbar.LENGTH_INDEFINITE);
				snackbar.setCallback(new Snackbar.Callback() {
					@Override
					public void onDismissed(Snackbar snackbar, int event) {
						super.onDismissed(snackbar, event);
						EnvironmentError.clear();
					}
				});

				//TODO: Make dismissing the snackbar work
			}

			if (lastError == null)
				snackbar.dismiss();
			else {
				snackbar.setText(lastError.message);
				snackbar.setAction("More", new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						try {
							lastError.showDialogIntent().send();
						} catch (PendingIntent.CanceledException e) {
							throw new RuntimeException(e);
						}
					}
				});
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
