package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class CompositeServiceStatusDisplay extends ServiceStatusDisplay {

	private final List<ServiceStatusDisplay> wrapped;

	//TODO: Don't show the notification unless it's actually enabled.
	public CompositeServiceStatusDisplay(Service service, ApplicationSettings appSettings) {
		wrapped = new ArrayList<>();

		if (appSettings.showNotification().get())
			wrapped.add(new NotificationServiceStatusDisplay(service, appSettings));
		if (QuickSettingServiceStatusDisplay.isSupported())
			wrapped.add(new QuickSettingServiceStatusDisplay());
	}

	@Override
	public void update(NotificationParameters params) {
		for (ServiceStatusDisplay display : wrapped)
			display.update(params);
	}

	@Override
	public Set<EventRegistrar> updateTriggers() {
		HashSet<EventRegistrar> result = new HashSet<>();

		for (ServiceStatusDisplay display : wrapped)
			result.addAll(display.updateTriggers());

		return result;
	}

	@Override
	public void cancel() {
		for (ServiceStatusDisplay display : wrapped)
			display.cancel();
	}

}
