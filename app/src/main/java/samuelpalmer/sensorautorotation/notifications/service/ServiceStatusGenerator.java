package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.processes.service.LockScreenMonitor;
import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;
import samuelpalmer.sensorautorotation.settings.application.Settings.Setting;

public class ServiceStatusGenerator {

	private final OrientationCalculator calculator;
	private final ScreenRotationMonitor screenRotationMonitor;
	private final NaturalOrientationDetector naturalOrientationDetector;
	private final ScreenOrientationUpdater screenOrientationUpdater;
	private final LockScreenMonitor lockScreenMonitor;
	private final Setting<NotificationPriority> priority;

	public ServiceStatusGenerator(OrientationCalculator calculator, ScreenRotationMonitor screenRotationMonitor, NaturalOrientationDetector naturalOrientationDetector, ScreenOrientationUpdater screenOrientationUpdater, LockScreenMonitor lockScreenMonitor) {
		this.calculator = calculator;
		this.screenRotationMonitor = screenRotationMonitor;
		this.naturalOrientationDetector = naturalOrientationDetector;
		this.screenOrientationUpdater = screenOrientationUpdater;
		this.lockScreenMonitor = lockScreenMonitor;
		priority = ApplicationSettings.local().notificationPriority();
	}

	public NotificationParameters generate() {
		return new NotificationParameters(
			mode(),
			overridden(),
			new UserOrientation(calculator.user(), screenRotationMonitor, calculator.proposedScreenOrientation()),
			priority.get(),
			lockScreenMonitor.isOnLockScreen()
		);
	}

	private Mode mode() {
		return calculator.autoRotate() ? new AutoRotationMode() : new LockedMode(calculator, naturalOrientationDetector.deviceIsNaturallyPortrait());
	}

	public boolean overridden() {
		Orientation requested = screenOrientationUpdater.proposedOrientation();
		Orientation actual = screenRotationMonitor.currentRotation();

		return !requested.equals(actual);
	}

}
