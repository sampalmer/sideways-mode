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

public class AccessibilityServiceManager extends State {

	private final NotificationManager notificationManager;
	private final Settings.Setting<Boolean> enabled;
	private final Settings.Setting<Set<String>> forcedApps;
	private final AccessibilityServiceEnabledStateMonitor serviceMonitor;

	public AccessibilityServiceManager() {
		notificationManager = SystemServices.notificationManager();

		enabled = ApplicationSettings.local().enabled();
		forcedApps = ApplicationSettings.local().forcedOrientationAppPackages();
		serviceMonitor = new AccessibilityServiceEnabledStateMonitor();
	}

	@Override
	protected void start() {
		enabled.subscribe(subscriber);
		forcedApps.subscribe(subscriber);
		serviceMonitor.subscribe(subscriber);

		check();
	}

	@Override
	protected void stop() {
		check();

		serviceMonitor.unsubscribe(subscriber);
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
		//TODO: Replace all size() > 0 checks with !isEmpty()
		if (enabled.get() && forcedApps.get().size() > 0 && !serviceMonitor.isEnabled()) {
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
