package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.os.Build;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.service.LockScreenMonitor;
import samuelpalmer.sensorautorotation.processes.service.NotificationException;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings.Setting;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class NotificationServiceStatusDisplay extends ServiceStatusDisplay {
	private final Service service;
	private final LockScreenMonitor lockScreenMonitor;
	private final Setting<NotificationPriority> priority;

	public NotificationServiceStatusDisplay(Service service, ApplicationSettings settings) {
		this.service = service;
		lockScreenMonitor = new LockScreenMonitor();
		priority = settings.notificationPriority();
	}

	public void update(NotificationParameters params) {
		ServiceNotification builder = new ServiceNotification(priority.get(), params);
		Notification notification = builder.build();

		cancelIfNecessary(notification);

		try {
			service.startForeground(NotificationIds.SERVICE, notification);
		} catch (Exception ex) {
			throw new NotificationException(ex);
		}
	}

	@Override
	public Set<EventRegistrar> updateTriggers() {
		Set<EventRegistrar> result = new HashSet<>();

		result.add(priority);
		if (hideFromLockScreen())
			result.add(lockScreenMonitor);

		return result;
	}

	@Override
	public void cancel() {
		NotificationManager notificationManager = SystemServices.notificationManager();

		try {
			service.stopForeground(true);
			notificationManager.cancel(NotificationIds.SERVICE); //Just in case
		} catch (Exception ex) {
			throw new NotificationException(ex);
		}
	}

	/**
	 * Turns out that the notification's presence in the status bar doesn't change when the
	 * notification is updated on these Android versions of the emulators and Samsung Galaxy S II
	 * (and probably other devices). Interestingly, the problem didn't happen on a Sony Xperia M
	 * running Android 4.3.
	 */
	private void cancelIfNecessary(Notification notification) {
		if (Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT <= 19) {
			if (lastPriority != null && notification.priority != lastPriority)
				cancel();
			lastPriority = notification.priority;
		}
	}

	private static boolean hideFromLockScreen() {
		return Build.VERSION.SDK_INT >= 21;
	}

	private Integer lastPriority;
}
