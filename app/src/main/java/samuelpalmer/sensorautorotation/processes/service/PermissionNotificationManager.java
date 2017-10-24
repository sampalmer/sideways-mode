package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.Set;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.service.orientationenforcement.ScreenOrientationEnforcer;
import samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement.ForcedAppsActivity;
import samuelpalmer.sensorautorotation.utilities.AccessibilityServiceEnabledStateMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

import static android.content.Context.NOTIFICATION_SERVICE;

//Note that it's possible that Samsung's app optimization feature might disable the accessibility service, although this might not occur since this is a foreground app (https://lastpass.com/support.php?cmd=showfaq&id=9272). Hopefully this notification will be sufficient for handling this.
public class PermissionNotificationManager extends State {

	private final NotificationManager notificationManager;
	private final SharedPreferences prefs;
	private final AccessibilityServiceEnabledStateMonitor accessibilityServiceMonitor;
	private final Context context;
	
	private boolean isRunning = false;
	
	public PermissionNotificationManager(Context context) {
		this.context = context;
		notificationManager = (NotificationManager) this.context.getSystemService(NOTIFICATION_SERVICE);
		
		prefs = ServiceSettings.getSharedPreferences(this.context);
		accessibilityServiceMonitor = new AccessibilityServiceEnabledStateMonitor(this.context, WindowChangeDetectingService.class);
	}

	@Override
	protected void start() {
		prefs.registerOnSharedPreferenceChangeListener(forcedAppsChanged);
		accessibilityServiceMonitor.subscribe(subscriber);
		
		isRunning = true;
		check();
}
	
	@Override
	protected void stop() {
		isRunning = false;
		check();
		
		accessibilityServiceMonitor.unsubscribe(subscriber);
		prefs.unregisterOnSharedPreferenceChangeListener(forcedAppsChanged);
	}

	private final SharedPreferences.OnSharedPreferenceChangeListener forcedAppsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			if (s.equals(ServiceSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES))
				check();
		}
	};
	
	private final EventHandler subscriber = new EventHandler() {
		@Override
		public void update() {
			check();
		}
	};
	
	public void check() {
		if (needsPermission()) {
			PendingIntent forcedAppsIntent = PendingIntent.getActivity(
			context,
				PendingIntentIds.FORCED_APPS_ACQUIRE_PERMISSION,
				new Intent(context, ForcedAppsActivity.class),
				PendingIntentIds.PENDING_INTENT_FLAGS);
			
			Notification notification = NotificationBuilderFactory
					.create(context)
					.setCategory(NotificationBuilderCompat.CATEGORY_ERROR)
					.setContentIntent(forcedAppsIntent)
					.setContentTitle(context.getText(R.string.permission_needed_summary))
					.setContentText(context.getText(R.string.permission_touch_to_fix))
					.setSmallIcon(R.drawable.notification_error)
					.setChannelId(NotificationChannels.ID_PERMISSIONS)
					.build();

			notificationManager.notify(NotificationIds.PERMISSION, notification);
		}
		else
			notificationManager.cancel(NotificationIds.PERMISSION);
	}
	
	private boolean needsPermission() {
		if (isRunning) {
			Set<String> forcedAppPackages = prefs.getStringSet(ServiceSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES, Collections.<String>emptySet());
			if (!forcedAppPackages.isEmpty() && !(accessibilityServiceMonitor.isEnabled() && ScreenOrientationEnforcer.hasPermissionToStart(context)))
				return true;
		}
		
		return false;
	}
	
}
