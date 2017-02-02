package samuelpalmer.sensorautorotation.processes.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemClock;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.SystemServices;

/**
 * Useful for when the service is killed due to out-of-memory conditions
 * such as when using a web browser.
 */
public class ServiceReviver extends State {

	private final int PERIOD_MILLISECONDS = 500;

	@Override
	protected void start() {
		SystemServices.alarmManager().setInexactRepeating(
				AlarmManager.ELAPSED_REALTIME,
				firstTriggerTimeInMilliseconds(),
				PERIOD_MILLISECONDS,
				intent()
		);
	}

	private long firstTriggerTimeInMilliseconds() {
		return SystemClock.elapsedRealtime() + PERIOD_MILLISECONDS;
	}

	private PendingIntent intent() {
		ComponentType componentType = ComponentType.create(ServiceSyncReceiver.class);
		return componentType.pendingIntent(PendingIntentIds.SERVICE_REVIVER);
	}

	@Override
	protected void stop() {
		SystemServices.alarmManager().cancel(intent());
	}

}
