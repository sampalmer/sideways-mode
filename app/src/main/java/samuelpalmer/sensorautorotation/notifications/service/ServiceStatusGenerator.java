package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.Screen;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ServiceStatusGenerator {

	private final OrientationCalculator calculator;
	private final Screen screen;
	private final ScreenRotationMonitor screenRotationMonitor;

	public ServiceStatusGenerator(OrientationCalculator calculator, Screen screen, ScreenRotationMonitor screenRotationMonitor) {
		this.calculator = calculator;
		this.screen = screen;
		this.screenRotationMonitor = screenRotationMonitor;
	}

	public NotificationParameters generate() {
		return new NotificationParameters(mode(), overridden(), new UserOrientation(calculator.user(), screenRotationMonitor, calculator.proposedScreenOrientation()));
	}

	private Mode mode() {
		return calculator.autoRotate() ? new AutoRotationMode() : new LockedMode(calculator, screen, screenRotationMonitor);
	}

	private boolean overridden() {
		Orientation requested = new SystemSettings().userRotation.get();
		Orientation actual = screenRotationMonitor.currentRotation();

		return !requested.equals(actual);
	}

}
