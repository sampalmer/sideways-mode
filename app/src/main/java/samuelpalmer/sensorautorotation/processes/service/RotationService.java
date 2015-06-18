package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Encapsulates both the settings server (for the UI) and the main background rotation logic.
 * They're both using this service since otherwise Android sometimes kills the main service when
 * the main activity task is removed.
 */
public class RotationService extends ServiceService {

	private MainSubservice main;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (main == null) {
			main = new MainSubservice(this);
			main.start();
		}

		return Service.START_STICKY;
	}

	public void stopMainSubservice() {
		ensureMainStopped();
		stopSelf();
	}

	private void ensureMainStopped() {
		if (main != null) {
			main.stop();
			main = null;
		}
	}

	private SettingsSubservice settings;

	@Override
	public IBinder onBind(Intent intent) {
		if (settings == null) {
			settings = new SettingsSubservice();
			settings.ensureStarted();
		}

		return settings.binder;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		ensureSettingsStopped();
		return super.onUnbind(intent);
	}

	private void ensureSettingsStopped() {
		if (settings != null) {
			settings.ensureStopped();
			settings = null;
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		ensureMainStopped();
		ensureSettingsStopped();
	}
}
