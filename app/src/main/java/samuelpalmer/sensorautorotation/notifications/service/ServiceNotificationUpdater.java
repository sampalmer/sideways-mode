package samuelpalmer.sensorautorotation.notifications.service;

import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.screen.Screen;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ServiceNotificationUpdater {

	private final ServiceNotificationPoster poster;
	private final ApplicationSettings settings;
	private final OrientationCalculator calculator;
	private final Screen screen;

	public ServiceNotificationUpdater(ServiceNotificationPoster poster, ApplicationSettings settings, OrientationCalculator calculator, Screen screen) {
		this.poster = poster;
		this.settings = settings;
		this.calculator = calculator;
		this.screen = screen;
	}

	public void update() {
		ServiceNotification builder = new ServiceNotification(priority(), mode(), overridden(), calculator.user());
		poster.post(builder.build());
	}

	private NotificationPriority priority() {
		return settings.notificationPriority().get();
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
