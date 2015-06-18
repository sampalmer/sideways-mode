package samuelpalmer.sensorautorotation.processes.service;

import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferencesServer;

public class SettingsSubservice extends State {

	@Override
	protected void start() {
		prefs = Prefs.prefs();
		binder = new RemoteSharedPreferencesServer(prefs);
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	protected void stop() {
		prefs.unregisterOnSharedPreferenceChangeListener(listener);
	}

	private SharedPreferences prefs;
	public RemoteSharedPreferencesServer binder;

	private final SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			binder.updateRemoteListeners(key);
		}
	};

}
