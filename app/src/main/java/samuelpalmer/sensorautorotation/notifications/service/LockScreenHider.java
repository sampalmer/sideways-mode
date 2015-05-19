package samuelpalmer.sensorautorotation.notifications.service;

import android.content.Context;
import android.os.Build;

import samuelpalmer.sensorautorotation.LockScreenMonitor;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

/**
 * Updates the service notification to try to prevent it from showing on the lock screen on Android 5
 */
public class LockScreenHider extends State {

	private final ServiceNotification serviceNotification;
	private final LockScreenMonitor detector;

	public LockScreenHider(Context context, ServiceNotification serviceNotification) {
		this.serviceNotification = serviceNotification;
		this.detector = new LockScreenMonitor(context);
	}

	@Override
	protected void start() {
		if (android5()) {
			detector.possiblyChanged().subscribe(subscriber);
			detector.ensureStarted();
		}
	}

	@Override
	protected void stop() {
		if (android5()) {
			detector.ensureStopped();
			detector.possiblyChanged().unsubscribe(subscriber);
		}
	}

	private boolean android5() {
		return Build.VERSION.SDK_INT >= 21;
	}

	private final EventHandler<Void> subscriber = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			serviceNotification.set();
		}
	};

}
