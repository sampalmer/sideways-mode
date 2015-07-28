package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.app.Service;
import android.os.Build;

import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class NotificationServiceStatusDisplay extends ServiceStatusDisplay {
	private final Service service;
	private final ApplicationSettings settings;

	public NotificationServiceStatusDisplay(Service service, ApplicationSettings settings) {
		this.service = service;
		this.settings = settings;
	}

	public void update(NotificationParameters params) {
		ServiceNotification builder = new ServiceNotification(priority(), params);
		Notification notification = builder.build();

		cancelIfNecessary(notification);
		service.startForeground(NotificationIds.SERVICE, notification);
	}

	private NotificationPriority priority() {
		return settings.notificationPriority().get();
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
				service.stopForeground(true);
				SystemServices.notificationManager().cancel(NotificationIds.SERVICE);
			}
			lastPriority = notification.priority;
		}
	}

	private Integer lastPriority;
}
