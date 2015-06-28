package samuelpalmer.sensorautorotation.processes.ui;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.SettingsService;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferences;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferencesAdaptor;

/**
 * UI proxy to settings stored in background process.
 */
public class MainActivity extends UiActivity {

	private static final String TAG_FIRST_RUN_FRAGMENT = "first run";

	private RemoteSharedPreferencesAdaptor preferences;

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			preferences = new RemoteSharedPreferencesAdaptor(RemoteSharedPreferences.Stub.asInterface(service));

			FirstRunLauncherFragment firstRunFragment = (FirstRunLauncherFragment) getFragmentManager().findFragmentByTag(TAG_FIRST_RUN_FRAGMENT);
			firstRunFragment.onPreferencesAvailable(preferences);

			SettingsFragment settings = (SettingsFragment) getFragmentManager().findFragmentById(R.id.settings);
			settings.onPreferencesAvailable(preferences);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			preferences = null;

			//We could have got here as a result of the user explicitly stopping the background service, so we shouldn't crash.
			//If the service crashed, there's not much benefit to the UI also crashing. Might as well gracefully exit.
			//Once the user re-opens the task, this activity should be created again anyway.
			finish();
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

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
	public SharedPreferences getSharedPreferences(String name, int mode) {
		//PreferenceFragment calls this for its underlying preference storage
		if (name.equals(ApplicationSettings.sharedPreferencesName())) {
			if (preferences == null)
				throw new RuntimeException("Preferences requested but not available.");

			return preferences;
		}
		else
			return super.getSharedPreferences(name, mode);
	}

}
