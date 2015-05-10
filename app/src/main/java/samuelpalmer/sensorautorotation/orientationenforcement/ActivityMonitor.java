package samuelpalmer.sensorautorotation.orientationenforcement;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ActivityMonitor {

	private final UpdatingEventRegistrar<Void> onChange = new UpdatingEventRegistrar<>();
	private String lastApp;

	public ActivityMonitor() {
		lastApp = "";
	}

	public String current() {
		return lastApp;
	}

	public void setCurrentApp(String currentApp) {
		if (!currentApp.equals(lastApp)) {
			lastApp = currentApp;
			onChange.report(null);
		}
	}

	public EventRegistrar<Void> onChange() {
		return onChange;
	}
}
