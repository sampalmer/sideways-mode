package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ComponentType;

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
		clearPendingDeath();

		Intent intent = ComponentType.create(EnsureAppLoadedReceiver.class).makeIntent();

		//This seems to be timing-related; the more times we do this, the less likely the process gets killed
		for (int i = 0; i < 50; ++i)
			sendBroadcast(intent);
	}

	/**
	 * Resets the process's "waitingToKill" flag so it won't be killed on the next received broadcast.
	 * This has a delayed effect since it only seems to apply once the activity has actually started.
	 */
	private void clearPendingDeath() {
		Intent intent = new Intent(this, DummyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}
