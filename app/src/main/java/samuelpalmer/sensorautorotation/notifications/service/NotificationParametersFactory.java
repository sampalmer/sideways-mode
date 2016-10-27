package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Build;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.NaturalOrientationDetector;
import samuelpalmer.common.Orientation;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.common.settings.application.Settings.Setting;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class NotificationParametersFactory {

	private final OrientationCalculator calculator;
	private final ScreenRotationMonitor screenRotationMonitor;
	private final NaturalOrientationDetector naturalOrientationDetector;
	private final ScreenOrientationUpdater screenOrientationUpdater;
	private final LockScreenMonitor lockScreenMonitor;
	private final Setting<NotificationPriority> priority;

	public NotificationParametersFactory(OrientationCalculator calculator, ScreenRotationMonitor screenRotationMonitor, NaturalOrientationDetector naturalOrientationDetector, ScreenOrientationUpdater screenOrientationUpdater, LockScreenMonitor lockScreenMonitor) {
		this.calculator = calculator;
		this.screenRotationMonitor = screenRotationMonitor;
		this.naturalOrientationDetector = naturalOrientationDetector;
		this.screenOrientationUpdater = screenOrientationUpdater;
		this.lockScreenMonitor = lockScreenMonitor;
		priority = ApplicationSettings.local().notificationPriority();
	}

	public Set<EventRegistrar> updateTriggers() {
		Set<EventRegistrar> result = new HashSet<>(
			Arrays.<EventRegistrar>asList(
				priority, //To change notification priority
				screenRotationMonitor, //For "overridden" detection
				calculator.onAnythingChanged //For "overridden" detection, user orientation changes, auto rotation mode changes
			)
		);

		if (hideFromLockScreen())
			result.add(lockScreenMonitor);

		return result;
	}

	public NotificationParameters generate() {
		return new NotificationParameters(
			mode(),
			overridden(),
			new UserOrientation(effectiveUser(), screenRotationMonitor, calculator.proposedScreenOrientation()),
			priority()
		);
	}

	private Mode mode() {
		return calculator.autoRotate() ? new AutoRotationMode() : new LockedMode(calculator, naturalOrientationDetector.deviceIsNaturallyPortrait());
	}

	private boolean overridden() {
		Orientation requested = screenOrientationUpdater.proposedOrientation();
		Orientation actual = screenRotationMonitor.currentRotation();

		return !requested.equals(actual);
	}

	/**
	 * Even if the user is technically upside-down, we swap them to upright
	 * once they go into auto-rotate mode, so from their perspective, they
	 * weren't actually upside-down.
	 */
	private Orientation effectiveUser() {
		Orientation result = calculator.user();
		return result.equals(Orientation.UPSIDE_DOWN) ? Orientation.NATURAL : result; //TODO: Move this logic inside OrientationCalculator
	}

	private NotificationPriority priority() {
		return lockScreenMonitor.isOnLockScreen() && hideFromLockScreen() ? NotificationPriority.LOW : priority.get();
	}

	private static boolean hideFromLockScreen() {
		return Build.VERSION.SDK_INT >= 21;
	}

}
