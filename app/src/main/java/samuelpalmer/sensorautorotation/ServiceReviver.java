package samuelpalmer.sensorautorotation;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.os.SystemClock;

import samuelpalmer.sensorautorotation.components.EnsureAppLoadedReceiver;

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
		Intents intents = new Intents(EnsureAppLoadedReceiver.class);
		return intents.makePending();
	}

	@Override
	protected void stop() {
		SystemServices.alarmManager().cancel(intent());
	}

}
