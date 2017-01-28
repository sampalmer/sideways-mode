package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Build;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.processes.service.QuickSettingStatusDisplay;
import samuelpalmer.sensorautorotation.processes.service.RotationService;
import samuelpalmer.sensorautorotation.processes.service.ServiceNotificationHider;
import samuelpalmer.sensorautorotation.processes.service.ServiceStatusContentProvider;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceStatusDisplays implements ServiceStatusDisplay {

	private final Set<ServiceStatusDisplay> displays = new HashSet<>();
	private final NotificationStatusDisplay notification;
	private final Settings.Setting<Boolean> showNotificationSetting;

	private ServiceStatus lastServiceStatus;

	public ServiceStatusDisplays(RotationService service) {
		notification = new NotificationStatusDisplay(service);

		showNotificationSetting = ApplicationSettings.local().showNotification();
		showNotificationSetting.subscribe(showNotificationSettingChanged);
		showNotificationSettingChanged.update();

		if (Build.VERSION.SDK_INT >= 24)
			add(new QuickSettingStatusDisplay());

		add(new ServiceStatusContentProvider.RemoteStatusDisplay());
	}

	private final EventHandler showNotificationSettingChanged = new EventHandler() {
		@Override
		public void update() {
			boolean showingNotification = showNotificationSetting.get();
			if (showingNotification || !ServiceNotificationHider.isSupported())
				add(notification);
			else {
				displays.remove(notification);
				notification.enterForegroundWithoutNotification();
			}
		}
	};

	public void add(ServiceStatusDisplay display) {
		displays.add(display);

		if (lastServiceStatus != null)
			display.update(lastServiceStatus);
	}

	public void remove(ServiceStatusDisplay display) {
		displays.remove(display);
	}

	@Override
	public void onServiceStopped() {
		for (ServiceStatusDisplay display : displays)
			display.onServiceStopped();

		showNotificationSetting.unsubscribe(showNotificationSettingChanged);
	}

	@Override
	public void update(ServiceStatus serviceStatus) {
		for (ServiceStatusDisplay display : displays)
			display.update(serviceStatus);

		lastServiceStatus = serviceStatus;
	}

}
