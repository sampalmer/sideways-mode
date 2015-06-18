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

	public void ensureRemoved() {
		service.stopForeground(true);
	}

	/**
	 * Turns out that the notification's presence in the status bar doesn't change when the
	 * notification is updated on these Android versions of the emulators and Samsung Galaxy S II
	 * (and probably other devices). Interestingly, the problem didn't happen on a Sony Xperia M
	 * running Android 4.3.
	 */
	private void cancelIfNecessary(Notification notification) {
		if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT <= 19) {
			if (lastPriority != null && notification.priority != lastPriority) {
				ensureRemoved();
				SystemServices.notificationManager().cancel(NotificationIds.SERVICE);
			}
			lastPriority = notification.priority;
		}
	}

	private Integer lastPriority;
}
