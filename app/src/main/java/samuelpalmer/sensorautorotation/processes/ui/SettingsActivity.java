package samuelpalmer.sensorautorotation.processes.ui;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.processes.service.SettingsService;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferences;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferencesAdaptor;

/**
 * UI proxy to settings stored in background process.
 */
public class SettingsActivity extends UiActivity {

	private static final String TAG_FIRST_RUN_FRAGMENT = "first run";

	private RemoteSharedPreferencesAdaptor preferences;

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			preferences = new RemoteSharedPreferencesAdaptor(RemoteSharedPreferences.Stub.asInterface(service));

			FirstRunLauncherFragment fragmentByTag = (FirstRunLauncherFragment) getFragmentManager().findFragmentByTag(TAG_FIRST_RUN_FRAGMENT);
			fragmentByTag.onPreferencesAvailable(preferences);

			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new SettingsFragment())
					.commit();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			//We could have got here as a result of the user explicitly stopping the background service, so we shouldn't crash.
			//If the service crashed, there's not much benefit to the UI also crashing. Might as well gracefully exit.
			//Once the user re-opens the task, this activity should be created again anyway.
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Prevents the fragment from automatically being recreated.
		// We need to do this since the fragment shouldn't exist without the service binding.
		super.onCreate(null);

		getFragmentManager()
				.beginTransaction()
				.add(new MainMenuFragment(), null)
				.add(new FirstRunLauncherFragment(), TAG_FIRST_RUN_FRAGMENT)
				.commit();

		bindService(ComponentType.create(SettingsService.class).intent(), serviceConnection, BIND_AUTO_CREATE);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(serviceConnection);
	}

	@Override
	protected void onSaveInstanceState(@SuppressWarnings("NullableProblems") Bundle outState) {
		//Don't invoke super.onSaveInstanceState since it'll prevent us from adding/removing the fragment while we're paused
	}

	@Override
	public SharedPreferences getSharedPreferences(String name, int mode) {
		//PreferenceFragment calls this for its underlying preference storage
		return name.equals(ApplicationSettings.sharedPreferencesName()) ? preferences : super.getSharedPreferences(name, mode);
	}

}
