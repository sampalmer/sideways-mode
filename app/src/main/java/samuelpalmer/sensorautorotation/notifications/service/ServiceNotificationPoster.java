package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.app.Service;
import android.os.Build;

import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;

public class ServiceNotificationPoster {
	private final Service service;

	public ServiceNotificationPoster(Service service) {
		this.service = service;
	}

	public void post(Notification notification) {
		cancelIfNecessary(notification);
		service.startForeground(NotificationIds.SERVICE, notification);
	}

	/**
	 * Turns out that the notification's presence in the status bar doesn't change when the
	 * notification is updated on these Android versions of the emulator.
	 */
	private void cancelIfNecessary(Notification notification) {
		if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT <= 19) {
			if (lastPriority != null && notification.priority != lastPriority) {
				service.stopForeground(true);
				SystemServices.notificationManager().cancel(NotificationIds.SERVICE);
			}
			lastPriority = notification.priority;
		}
	}

	private Integer lastPriority;
}
