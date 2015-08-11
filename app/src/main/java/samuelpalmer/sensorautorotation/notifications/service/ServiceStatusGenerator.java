package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.Screen;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ServiceStatusGenerator {

	private final OrientationCalculator calculator;
	private final Screen screen;

	public ServiceStatusGenerator(OrientationCalculator calculator, Screen screen) {
		this.calculator = calculator;
		this.screen = screen;
	}

	public void update(ServiceStatusDisplay display) {
		Mode mode = mode();
		NotificationParameters params = new NotificationParameters(mode, overridden(), getSummary(mode));
		display.update(params);
	}

	public CharSequence getSummary(Mode mode) {
		return new CurrentStateSummary(mode, calculator.user()).buildTitle();
	}

	public Mode mode() {
		return calculator.autoRotate() ? new AutoRotationMode() : new LockedMode(calculator, screen);
	}

	private boolean overridden() {
		Orientation requested = new SystemSettings().userRotation.get();
		Orientation actual = screen.realOrientation();

		return !requested.equals(actual);
	}

}
