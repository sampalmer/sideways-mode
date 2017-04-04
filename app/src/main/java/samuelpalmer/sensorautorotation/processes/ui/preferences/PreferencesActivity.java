package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.view.ViewGroup;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.ui.UiActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

/**
 * UI proxy to settings stored in background process.
 * Also supports showing an error message at the bottom of the activity.
 */
public abstract class PreferencesActivity extends UiActivity {

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
				View contentView = findViewById(R.id.coordinator_layout);
				if (contentView == null)
					throw new RuntimeException("Didn't find any elements with the coordinator layout id");

				if (!(contentView instanceof CoordinatorLayout))
					throw new RuntimeException("Content layout should be a " + CoordinatorLayout.class.getSimpleName() + " so the environment error snackbar can be dismissed. Instead found a " + contentView.getClass().getName() + ".");

				snackbar = Snackbar.make(contentView, "", Snackbar.LENGTH_INDEFINITE);
				snackbar.setCallback(new Snackbar.Callback() {
					@Override
					public void onDismissed(Snackbar snackbar, int event) {
						super.onDismissed(snackbar, event);

						if (event == DISMISS_EVENT_SWIPE || event == DISMISS_EVENT_ACTION)
							EnvironmentError.clear();

						//If the user swipes away the snackbar, it can't be shown anymore, so this one is effectively dead
						PreferencesActivity.this.snackbar = null;
					}
				});
			}

			if (lastError == null)
				snackbar.dismiss();
			else {
				snackbar.setText(lastError.message);
				snackbar.setAction(R.string.environment_error_more, new View.OnClickListener() {
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
