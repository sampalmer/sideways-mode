package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Build;

import samuelpalmer.sensorautorotation.LockScreenMonitor;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

/**
 * Updates the service notification to try to prevent it from showing on the lock screen on Android 5
 */
public class LockScreenHider extends State {

	private final LockScreenMonitor detector;
	private final EventHandler<Void> handleChange;

	public LockScreenHider(EventHandler<Void> handleChange) {
		this.handleChange = handleChange;
		this.detector = new LockScreenMonitor();
	}

	@Override
	protected void start() {
		if (android5()) {
			detector.possiblyChanged().subscribe(handleChange);
			detector.ensureStarted();
		}
	}

	@Override
	protected void stop() {
		if (android5()) {
			detector.ensureStopped();
			detector.possiblyChanged().unsubscribe(handleChange);
		}
	}

	private boolean android5() {
		return Build.VERSION.SDK_INT >= 21;
	}

}
