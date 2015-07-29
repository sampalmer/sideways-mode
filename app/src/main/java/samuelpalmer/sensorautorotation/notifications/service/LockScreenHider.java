package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Build;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.processes.service.LockScreenMonitor;

/**
 * Updates the service notification to try to prevent it from showing on the lock screen on Android 5
 */
public class LockScreenHider extends State {

	private final LockScreenMonitor detector;

	public LockScreenHider() {
		this.detector = new LockScreenMonitor();
	}

	public EventRegistrar onChange() {
		return detector.possiblyChanged();
	}

	@Override
	protected void start() {
		if (needed())
			detector.ensureStarted();
	}

	@Override
	protected void stop() {
		if (needed())
			detector.ensureStopped();
	}

	private boolean needed() {
		return Build.VERSION.SDK_INT >= 21;
	}

}
