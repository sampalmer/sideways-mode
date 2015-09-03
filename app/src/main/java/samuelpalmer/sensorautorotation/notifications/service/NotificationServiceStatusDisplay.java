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
import samuelpalmer.sensorautorotation.processes.service.LockScreenMonitor;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;
import samuelpalmer.sensorautorotation.settings.application.Settings.Setting;

public class NotificationServiceStatusDisplay {
	private final Service service;
	private final ScreenOrientationUpdater screenOrientationUpdater;
	private final OrientationCalculator calculator;
	private final LockScreenMonitor lockScreenMonitor;
	private final Setting<NotificationPriority> priority;
	private final ScreenRotationMonitor screenRotationMonitor;

	public NotificationServiceStatusDisplay(Service service, ScreenOrientationUpdater screenOrientationUpdater, OrientationCalculator calculator, ScreenRotationMonitor screen, LockScreenMonitor lockScreenMonitor) {
		this.service = service;
		this.screenOrientationUpdater = screenOrientationUpdater;
		this.calculator = calculator;
		this.lockScreenMonitor = lockScreenMonitor;
		priority = ApplicationSettings.local().notificationPriority();
		screenRotationMonitor = screen;
	}

	public void update(NotificationParameters params) {
		ServiceNotification builder = new ServiceNotification(priority.get(), params);
		Notification notification = builder.build();

		cancelIfNecessary(notification);
		service.startForeground(NotificationIds.SERVICE, notification);
	}

	public Set<EventRegistrar> updateTriggers() {
		Set<EventRegistrar> result = new HashSet<>(
			Arrays.asList(
				priority, //To change notification priority
				screenRotationMonitor, //For "overridden" detection
				screenOrientationUpdater.onProposedOrientationChanged(), //For "overridden" detection
				calculator.onChange() //For mode change detection
			)
		);

		if (hideFromLockScreen())
			result.add(lockScreenMonitor);

		return result;
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

	private static boolean hideFromLockScreen() {
		return Build.VERSION.SDK_INT >= 21;
	}

	private Integer lastPriority;
}
