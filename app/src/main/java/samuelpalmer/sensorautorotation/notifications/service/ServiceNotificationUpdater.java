package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.Screen;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ServiceNotificationUpdater {

	private final ServiceStatusDisplay display;
	private final OrientationCalculator calculator;
	private final Screen screen;

	public ServiceNotificationUpdater(ServiceStatusDisplay display, OrientationCalculator calculator, Screen screen) {
		this.display = display;
		this.calculator = calculator;
		this.screen = screen;
	}

	public void update() {
		Mode mode = mode();
		NotificationParameters params = new NotificationParameters(mode, overridden(), getSummary(mode));

		display.update(params);
	}

	public void cancel() {
		display.cancel();
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
