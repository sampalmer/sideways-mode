package samuelpalmer.sensorautorotation.processes.ui.preferences.notifications;

import android.annotation.SuppressLint;
import android.app.NotificationManager;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.SystemServices;
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

	@SuppressLint("NewApi")
	public CharSequence getSummary() {
		if (ServiceNotificationHider.isSupported()) {
			if (showNotificationSetting.get())
				return getNotificationPriority();
			else
				return StringResource.get(R.string.preference_notification_summary_hidden);
		}
		else {
			if (notificationManager.areNotificationsEnabled())
				return getNotificationPriority();
			else
				return StringResource.get(R.string.preference_notification_summary_blocked);
		}
	}

	private CharSequence getNotificationPriority() {
		return StringResource.format(R.string.preference_notification_summary_priority, notificationPrioritySetting.get().stringResourceId);
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
