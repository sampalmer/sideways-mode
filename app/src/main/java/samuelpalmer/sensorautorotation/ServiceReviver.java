package samuelpalmer.sensorautorotation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.SystemClock;

import samuelpalmer.sensorautorotation.components.StartServiceReceiver;

/**
 * Useful for when the service is killed due to out-of-memory conditions
 * such as when using a web browser.
 */
public class ServiceReviver extends State {

	private final int PERIOD_MILLISECONDS = 500;

	@Override
	protected void start() {
		alarmManager().setInexactRepeating(
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
		Intents intents = new Intents(StartServiceReceiver.class);
		return intents.makePending();
	}

	@Override
	protected void stop() {
		alarmManager().cancel(intent());
	}

	private AlarmManager alarmManager() {
		return (AlarmManager) ThisApplication.context().getSystemService(Context.ALARM_SERVICE);
	}
}
