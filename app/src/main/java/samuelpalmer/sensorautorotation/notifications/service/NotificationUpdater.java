package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.app.Service;
import android.os.Build;

import samuelpalmer.sensorautorotation.notifications.NotificationIds;

public class NotificationUpdater {
	private final Service service;
	private ServiceNotification builder;

	public NotificationUpdater(Service service) {
		this.service = service;
		builder = new ServiceNotification();
	}

	public void update(NotificationParameters params) {
		if (lastNotificationParams == null || !params.equals(lastNotificationParams)) {
			Notification notification = builder.build(params);

			cancelIfNecessary(notification);
			service.startForeground(NotificationIds.SERVICE, notification);

			lastNotificationParams = params;
		}
	}

	private NotificationParameters lastNotificationParams;

	/**
	 * Turns out that the notification's presence in the status bar doesn't change when the
	 * notification is updated on API 16 - 19 on the emulators and Samsung Galaxy S II
	 * (and probably other devices). Interestingly, the problem didn't happen on a Sony Xperia M
	 * running Android 4.3.
	 *
	 * This also semes to fix an occasional bug that causes the notification's main content to be
	 * temporarily blank when the user opens or closes the lock screen.
	 */
	private void cancelIfNecessary(Notification notification) {
		if (Build.VERSION.SDK_INT >= 16) {
			if (lastPriority != null && notification.priority != lastPriority)
				service.stopForeground(true);
			lastPriority = notification.priority;
		}
	}

	private Integer lastPriority;
}
