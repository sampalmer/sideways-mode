package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Notification;
import android.app.Service;
import android.os.Build;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings.Setting;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class NotificationServiceStatusDisplay extends ServiceStatusDisplay {
	private final Service service;
	private final LockScreenHider notificationHider;
	private final Setting<NotificationPriority> priority;

	public NotificationServiceStatusDisplay(Service service, ApplicationSettings settings) {
		this.service = service;
		notificationHider = new LockScreenHider();
		priority = settings.notificationPriority();
	}

	@Override
	protected void start() {
		notificationHider.ensureStarted();
	}

	@Override
	protected void stop() {
		notificationHider.ensureStopped();
	}

	public void update(NotificationParameters params) {
		ServiceNotification builder = new ServiceNotification(priority.get(), params);
		Notification notification = builder.build();

		cancelIfNecessary(notification);
		service.startForeground(NotificationIds.SERVICE, notification);
	}

	@Override
	public Set<EventRegistrar> updateTriggers() {
		return new HashSet<>(Arrays.asList(
			notificationHider.onChange(),
			priority.onChange()
		));
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
