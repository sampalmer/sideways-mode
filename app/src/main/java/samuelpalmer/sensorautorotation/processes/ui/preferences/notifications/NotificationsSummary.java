package samuelpalmer.sensorautorotation.processes.ui.preferences.notifications;

import android.app.NotificationManager;
import android.os.Build;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.eventhandling.EventRegistrarAdaptor;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.service.ServiceNotificationHider;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.NotificationPriority;

public class NotificationsSummary extends EventRegistrarAdaptor {

	private final Settings.Setting<NotificationPriority> notificationPrioritySetting;

	public NotificationsSummary() {
		ApplicationSettings settings = ApplicationSettings.remote();
		notificationPrioritySetting = settings.notificationPriority();
	}

	public CharSequence getSummary() {
		if (NotificationsFragment.areNotificationsEnabled(CurrentProcess.context()) == Boolean.FALSE || dashboardNotificationHidden())
			return StringResource.get(R.string.off);
		else if (notificationPrioritySupported())
			return StringResource.format(R.string.preference_notification_summary_priority, notificationPrioritySetting.get().stringResourceId);
		else
			return StringResource.get(R.string.on);
	}
	
	private boolean dashboardNotificationHidden() {
		return
			(Build.VERSION.SDK_INT >= 26 || Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT >= 1)
			&&
			notificationManager.getNotificationChannel(NotificationChannels.ID_CONTROLS).getImportance() == NotificationManager.IMPORTANCE_NONE
		;
	}
	
	private boolean notificationPrioritySupported() {
		return !(Build.VERSION.SDK_INT >= 26 || Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT >= 1);
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
