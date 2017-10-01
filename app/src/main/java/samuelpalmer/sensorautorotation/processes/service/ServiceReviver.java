package samuelpalmer.sensorautorotation.processes.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.SystemClock;

import samuelpalmer.sensorautorotation.BroadcastKillFix;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.State;

import static android.content.Context.ALARM_SERVICE;

/**
 * Useful for when the service is killed due to out-of-memory conditions
 * such as when using a web browser.
 */
public class ServiceReviver extends State {

	private final int PERIOD_MILLISECONDS = 500;

	@Override
	protected void start() {
		((AlarmManager) CurrentProcess.context().getSystemService(ALARM_SERVICE)).setInexactRepeating(
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
		Intent intent = new Intent(CurrentProcess.context(), ServiceSyncReceiver.class);
		BroadcastKillFix.apply(intent);
		
		return PendingIntent.getBroadcast(
			CurrentProcess.context(),
			PendingIntentIds.SERVICE_REVIVER,
			intent,
			PendingIntentIds.PENDING_INTENT_FLAGS);
	}

	@Override
	protected void stop() {
		((AlarmManager) CurrentProcess.context().getSystemService(ALARM_SERVICE)).cancel(intent());
	}

}
