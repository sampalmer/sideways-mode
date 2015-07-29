package samuelpalmer.sensorautorotation.notifications.service;

import android.app.Service;

import java.util.ArrayList;
import java.util.List;

import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class CompositeServiceStatusDisplay extends ServiceStatusDisplay {

	private final List<ServiceStatusDisplay> wrapped;

	public CompositeServiceStatusDisplay(Service service, ApplicationSettings settings) {
		wrapped = new ArrayList<>();

		wrapped.add(new NotificationServiceStatusDisplay(service, settings));
		if (QuickSettingServiceStatusDisplay.isSupported())
			wrapped.add(new QuickSettingServiceStatusDisplay());
	}

	@Override
	public void update(NotificationParameters params) {
		for (ServiceStatusDisplay display : wrapped)
			display.update(params);
	}

	@Override
	public void cancel() {
		for (ServiceStatusDisplay display : wrapped)
			display.cancel();
	}

}
