package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Service;

import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.screen.Screen;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ServiceNotificationUpdater {

	private final Service service;
	private final ApplicationSettings settings;
	private final OrientationCalculator calculator;
	private final Screen screen;

	public ServiceNotificationUpdater(Service service, ApplicationSettings settings, OrientationCalculator calculator, Screen screen) {
		this.service = service;
		this.settings = settings;
		this.calculator = calculator;
		this.screen = screen;
	}

	public void update() {
		ServiceNotification notification = new ServiceNotification(priority(), mode(), overridden(), calculator.user());
		service.startForeground(NotificationIds.SERVICE, notification.build());
	}

	private NotificationPriority priority() {
		return settings.notificationPriority.get();
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
