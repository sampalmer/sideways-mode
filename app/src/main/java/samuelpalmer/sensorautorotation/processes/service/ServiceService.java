package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;

abstract class ServiceService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		ServiceProcess.ensureInitialised();
	}

	/**
	 * Works around a bug in swiping away the UI makes Android kill this process immediately or
	 * later on (such as when it receives a broadcast).
	 */
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Intent intent = new Intent(this, DummyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}
}
