package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;

import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement.ForcedAppsActivity;

import static android.content.Context.NOTIFICATION_SERVICE;

//Note that it's possible that Samsung's app optimization feature might disable the accessibility service, although this might not occur since this is a foreground app (https://lastpass.com/support.php?cmd=showfaq&id=9272). Hopefully this notification will be sufficient for handling this.
public class PermissionNotificationManager extends State {

	private final NotificationManager notificationManager;
	private final Settings.Setting<Set<String>> forcedApps;
	private final AccessibilityServiceEnabledStateMonitor accessibilityServiceMonitor;
	
	private boolean isRunning = false;
	
	public PermissionNotificationManager() {
		notificationManager = (NotificationManager) CurrentProcess.context().getSystemService(NOTIFICATION_SERVICE);
		
		forcedApps = ServiceSettings.instance().forcedOrientationAppPackages();
		accessibilityServiceMonitor = new AccessibilityServiceEnabledStateMonitor();
	}

	@Override
	protected void start() {
		forcedApps.subscribe(subscriber);
		accessibilityServiceMonitor.subscribe(subscriber);
		
		isRunning = true;
		check();
	}

	@Override
	protected void stop() {
		isRunning = false;
		check();

		accessibilityServiceMonitor.unsubscribe(subscriber);
		forcedApps.unsubscribe(subscriber);
	}

	private final EventHandler subscriber = new EventHandler() {
		@Override
		public void update() {
			check();
		}
	};

	public void check() {
		if (isRunning && !forcedApps.get().isEmpty() && !(accessibilityServiceMonitor.isEnabled() && ScreenOrientationEnforcer.hasPermissionToStart(CurrentProcess.context()))) {
			PendingIntent forcedAppsIntent = PendingIntent.getActivity(
				CurrentProcess.context(),
				PendingIntentIds.FORCED_APPS_ACQUIRE_PERMISSION,
				new Intent(CurrentProcess.context(), ForcedAppsActivity.class),
				PendingIntentIds.PENDING_INTENT_FLAGS);
			
			Notification notification = NotificationBuilderFactory
					.create()
					.setCategory(NotificationBuilderCompat.CATEGORY_ERROR)
					.setContentIntent(forcedAppsIntent)
					.setContentTitle(CurrentProcess.context().getText(R.string.permission_needed_summary))
					.setContentText(CurrentProcess.context().getText(R.string.permission_touch_to_fix))
					.setSmallIcon(R.drawable.notification_error)
					.setChannelId(NotificationChannels.ID_PERMISSIONS)
					.build();

			notificationManager.notify(NotificationIds.PERMISSION, notification);
		}
		else
			notificationManager.cancel(NotificationIds.PERMISSION);
	}

}
