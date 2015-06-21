package samuelpalmer.sensorautorotation.processes.service;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;

import samuelpalmer.sensorautorotation.settings.application.remote.RemoteSharedPreferencesServer;

public class SettingsService extends ServiceService {
	private SharedPreferences prefs;
	private RemoteSharedPreferencesServer binder;

	@Override
	public void onCreate() {
		super.onCreate();
		prefs = Prefs.prefs();
		binder = new RemoteSharedPreferencesServer(prefs);
		prefs.registerOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		prefs.unregisterOnSharedPreferenceChangeListener(listener);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		//Since the client isn't using BIND_AUTO_CREATE because it causes the background process to
		//be killed when the UI task is removed.
		stopSelf();
		return false;
	}

	private final SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			binder.updateRemoteListeners(key);
		}
	};
}
