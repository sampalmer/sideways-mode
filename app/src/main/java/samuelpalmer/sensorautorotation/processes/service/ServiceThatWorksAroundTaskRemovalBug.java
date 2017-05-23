package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ComponentType;

abstract class ServiceThatWorksAroundTaskRemovalBug extends Service {

	@Override
	public void onTaskRemoved(Intent rootIntent) {
		clearImmediateDeath();
		clearPendingDeath();
	}

	/**
	 * Works around a bug in which swiping away the UI makes Android kill this process immediately.
	 *
	 * https://code.google.com/p/android/issues/detail?id=178057
	 */
	private void clearImmediateDeath() {
		// Some users got ANRs when the broadcast receiver actually did work,
		// so now we just use a receiver that doesn't do anything.
		Intent intent = ComponentType.create(DummyReceiver.class).intent();

		//This seems to be timing-related; the more times we do this, the less likely the process gets killed
		for (int i = 0; i < 50; ++i)
			sendBroadcast(intent);
	}

	/**
	 * Resets the process's "waitingToKill" flag so it won't be killed on the next received broadcast.
	 * This has a delayed effect since it only seems to apply once the activity has actually started.
	 *
	 * https://code.google.com/p/android/issues/detail?id=104308
	 */
	private void clearPendingDeath() {
		Intent intent = new Intent(this, DummyActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intent);
	}

}
