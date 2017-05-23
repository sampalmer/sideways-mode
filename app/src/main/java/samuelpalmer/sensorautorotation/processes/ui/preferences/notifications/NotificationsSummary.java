package samuelpalmer.sensorautorotation.processes.ui.preferences.notifications;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class NotificationsSummary extends EventRegistrarAdaptor {

	private final Settings.Setting<NotificationPriority> notificationPrioritySetting;

	public NotificationsSummary() {
		ApplicationSettings settings = ApplicationSettings.remote();
		notificationPrioritySetting = settings.notificationPriority();
	}

	public CharSequence getSummary() {
		if (NotificationsFragment.areNotificationsEnabled(CurrentProcess.context()) == Boolean.FALSE)
			return StringResource.get(R.string.off);
		else
			return getNotificationPriority();
	}

	private CharSequence getNotificationPriority() {
		return StringResource.format(R.string.preference_notification_summary_priority, notificationPrioritySetting.get().stringResourceId);
	}

	@Override
	protected void subscribeToAdaptee() {
		notificationPrioritySetting.subscribe(somethingChanged);
	}

	@Override
	protected void unsubscribeFromAdaptee() {
		notificationPrioritySetting.unsubscribe(somethingChanged);
	}

	private final EventHandler somethingChanged = new EventHandler() {
		@Override
		public void update() {
			report();
		}
	};

}
