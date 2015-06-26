package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;

import samuelpalmer.sensorautorotation.Intents;

abstract class ServiceService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		ServiceProcess.ensureInitialised();
	}

	/**
	 * Works around a bug in which swiping away the UI makes Android kill this process immediately or
	 * later on (such as when it receives a broadcast).
	 */
	@Override
	public void onTaskRemoved(Intent rootIntent) {

		Intent intent = new Intents(EnsureAppLoadedReceiver.class).makeIntent();

		//This seems to be timing-related; the more times we do this, the less likely the process gets killed
		for (int i = 0; i < 50; ++i)
			sendBroadcast(intent);
	}

}
