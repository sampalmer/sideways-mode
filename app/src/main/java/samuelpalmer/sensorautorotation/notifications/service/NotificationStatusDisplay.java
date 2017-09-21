package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.app.Service;

import samuelpalmer.sensorautorotation.notifications.NotificationIds;

public class NotificationStatusDisplay implements ApplicationStatusDisplay {
	private final Service service;
	private final ServiceNotification builder;

	public NotificationStatusDisplay(Service service) {
		this.service = service;
		builder = new ServiceNotification();
	}

	@Override
	public void onApplicationStopped() {}

	public void update(ApplicationStatus applicationStatus) {
		Notification notification = builder.build(applicationStatus);
		
		// Running the service in the foreground reduces the chance of the process being killed.
		// This is important, since the process being killed would result in the screen no longer rotating.
		service.startForeground(NotificationIds.SERVICE, notification);
	}
	
}
