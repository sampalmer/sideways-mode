package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Build;
import android.support.annotation.NonNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.Orientation;
import samuelpalmer.common.eventhandling.EventRegistrar;
import samuelpalmer.common.settings.application.Settings.Setting;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class ApplicationStatusFactory {

	private final OrientationCalculator calculator;
	private final ScreenRotationMonitor screenRotationMonitor;
	private final ScreenOrientationUpdater screenOrientationUpdater;
	private final LockScreenMonitor lockScreenMonitor;
	private final Setting<NotificationPriority> priority;

	public ApplicationStatusFactory(OrientationCalculator calculator, ScreenRotationMonitor screenRotationMonitor, ScreenOrientationUpdater screenOrientationUpdater, LockScreenMonitor lockScreenMonitor) {
		this.calculator = calculator;
		this.screenRotationMonitor = screenRotationMonitor;
		this.screenOrientationUpdater = screenOrientationUpdater;
		this.lockScreenMonitor = lockScreenMonitor;
		priority = ApplicationSettings.instance().notificationPriority();
	}

	public Set<EventRegistrar> updateTriggers() {
		Set<EventRegistrar> result = new HashSet<>(
			Arrays.<EventRegistrar>asList(
				priority, //To change notification priority
				screenRotationMonitor, //For "overridden" detection
				calculator.onAnythingChanged //For "overridden" detection, user orientation changes, auto rotation mode changes, waiting on sensor changes
			)
		);

		if (hideFromLockScreen())
			result.add(lockScreenMonitor);

		return result;
	}

	public ApplicationStatus generate() {
		return new ApplicationStatus(
			new UserOrientation(effectiveUser(), screenRotationMonitor, calculator.proposedScreenOrientation()),
			priority(),
			calculator.isWaitingForSensor()
		);
	}
	
	/**
	 * If the app thinks the user is upside-down, it's more likely that they're upright but
	 * using the phone slightly tilted away from them. So we present being upside-down as being
	 * upright.
	 */
	private @NonNull Orientation effectiveUser() {
		Orientation actualUser = calculator.user();
		return actualUser.equals(Orientation.UPSIDE_DOWN) ? Orientation.NATURAL : actualUser;
	}

	private NotificationPriority priority() {
		return lockScreenMonitor.isOnLockScreen() && hideFromLockScreen() ? NotificationPriority.LOW : priority.get();
	}

	private static boolean hideFromLockScreen() {
		return Build.VERSION.SDK_INT >= 21;
	}

}
