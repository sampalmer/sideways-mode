package samuelpalmer.sensorautorotation.processes.ui;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.processes.service.SettingsService;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferences;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferencesAdaptor;

/**
 * UI proxy to settings stored in background process.
 */
public class SettingsActivity extends ServiceSyncActivity {

	private static final int REQUEST_CODE_INSTRUCT_USER = 1;

	private RemoteSharedPreferencesAdaptor preferences;
	private ApplicationSettings.Setting<Integer> instructedVersion;
	private boolean userInstructed;

	private final ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			preferences = new RemoteSharedPreferencesAdaptor(RemoteSharedPreferences.Stub.asInterface(service));
			getFragmentManager().beginTransaction()
					.replace(android.R.id.content, new SettingsFragment())
					.commit();

			instructUserIfNecessary();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			//We could have got here as a result of the user explicitly stopping the background service, so we shouldn't crash.
			//If the service crashed, there's not much benefit to the UI also crashing. Might as well gracefully exit.
			//Once the user re-opens the task, this activity should be created again anyway.
			finish();
		}
	};

	private void instructUserIfNecessary() {
		instructedVersion = new ApplicationSettings(preferences).lastVersionUserInstructedAbout();
		if (!instructedVersion.hasValue())
			if (userInstructed)
				recordUserInstructed();
			else
				startActivityForResult(ComponentType.create(FirstRunActivity.class).makeIntent(), REQUEST_CODE_INSTRUCT_USER);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_INSTRUCT_USER && resultCode == RESULT_OK) {
			userInstructed = true;
			if (instructedVersion != null)
				recordUserInstructed();
		}
	}

	private void recordUserInstructed() {
		instructedVersion.setAndCommit(VersionInfo.installedVersionCode());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// Prevents the fragment from automatically being recreated.
		// We need to do this since the fragment shouldn't exist without the service binding.
		super.onCreate(null);

		getFragmentManager()
				.beginTransaction()
				.add(new MainMenuFragment(), null)
				.commit();

		bindService(ComponentType.create(SettingsService.class).makeIntent(), serviceConnection, BIND_AUTO_CREATE);
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
