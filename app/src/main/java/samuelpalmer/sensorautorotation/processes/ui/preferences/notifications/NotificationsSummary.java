package samuelpalmer.sensorautorotation.processes.ui.preferences.notifications;

import android.app.NotificationManager;
import android.os.Build;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.service.ServiceNotificationHider;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class NotificationsSummary extends EventRegistrarAdaptor {

	private final Settings.Setting<NotificationPriority> notificationPrioritySetting;
	private final Settings.Setting<Boolean> showNotificationSetting;
	private final NotificationManager notificationManager;

	public NotificationsSummary() {
		ApplicationSettings settings = ApplicationSettings.remote();
		notificationPrioritySetting = settings.notificationPriority();
		showNotificationSetting = settings.showNotification();
		notificationManager = SystemServices.notificationManager();
	}

	public CharSequence getSummary() {
		if (allNotificationsBlocked())
			return StringResource.get(R.string.preference_notification_summary_blocked);
		else if (dashboardNotificationHidden())
			return StringResource.get(R.string.preference_notification_summary_hidden);
		else if (notificationPrioritySupported())
			return StringResource.format(R.string.preference_notification_summary_priority, notificationPrioritySetting.get().stringResourceId);
		else
			return StringResource.get(R.string.switchbar_on);
	}

	private boolean allNotificationsBlocked() {
		return Build.VERSION.SDK_INT >= 24 && !notificationManager.areNotificationsEnabled();
	}

	private boolean dashboardNotificationHidden() {
		return
				ServiceNotificationHider.isSupported() && !showNotificationSetting.get()
				||
				(
					(Build.VERSION.SDK_INT >= 26 || Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT >= 1)
					&&
					notificationManager.getNotificationChannel(NotificationChannels.ID_DASHBOARD).getImportance() == NotificationManager.IMPORTANCE_NONE
				);
	}

	private boolean notificationPrioritySupported() {
		return !(Build.VERSION.SDK_INT >= 26 || Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT >= 1);
	}

	@Override
	protected void subscribeToAdaptee() {
		notificationPrioritySetting.subscribe(somethingChanged);
		showNotificationSetting.subscribe(somethingChanged);
	}

	@Override
	protected void unsubscribeFromAdaptee() {
		showNotificationSetting.unsubscribe(somethingChanged);
		notificationPrioritySetting.unsubscribe(somethingChanged);
	}

	private final EventHandler somethingChanged = new EventHandler() {
		@Override
		public void update() {
			report();
		}
	};

}
