package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.NotificationManager;

import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.ui.orientationenforcement.ForcedAppsActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

//Note that it's possible that Samsung's app optimization feature might disable the accessibility service, although this might not occur since this is a foreground app (https://lastpass.com/support.php?cmd=showfaq&id=9272). Hopefully this notification will be sufficient for handling this.
public class AccessibilityServiceManager extends State {

	private final NotificationManager notificationManager;
	private final Settings.Setting<Boolean> enabled;
	private final Settings.Setting<Set<String>> forcedApps;
	private final AccessibilityServiceEnabledStateMonitor accessibilityServiceMonitor;

	public AccessibilityServiceManager() {
		notificationManager = SystemServices.notificationManager();

		enabled = ApplicationSettings.local().enabled();
		forcedApps = ApplicationSettings.local().forcedOrientationAppPackages();
		accessibilityServiceMonitor = new AccessibilityServiceEnabledStateMonitor();
	}

	@Override
	protected void start() {
		enabled.subscribe(subscriber);
		forcedApps.subscribe(subscriber);
		accessibilityServiceMonitor.subscribe(subscriber);

		check();
	}

	@Override
	protected void stop() {
		check();

		accessibilityServiceMonitor.unsubscribe(subscriber);
		forcedApps.unsubscribe(subscriber);
		enabled.unsubscribe(subscriber);
	}

	private final EventHandler subscriber = new EventHandler() {
		@Override
		public void update() {
			check();
		}
	};

	private void check() {
		if (enabled.get() && !forcedApps.get().isEmpty() && !accessibilityServiceMonitor.isEnabled()) {
			Notification notification = NotificationBuilderFactory
					.create()
					.setCategory(NotificationBuilderCompat.CATEGORY_ERROR)
					.setContentIntent(ComponentType.create(ForcedAppsActivity.class).pendingIntent())
					.setContentTitle(StringResource.get(R.string.warning_accessibility_service_disabled_title))
					.setContentText(StringResource.get(R.string.warning_accessibility_service_disabled_content))
					.setSmallIcon(R.drawable.error)
					.build();

			notificationManager.notify(NotificationIds.ACCESSIBILITY, notification);
		}
		else
			notificationManager.cancel(NotificationIds.ACCESSIBILITY);
	}

}
