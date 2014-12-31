package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ActivityMonitor {

	private RecentWindows mostRecentWindows = new RecentWindows();
	private final UpdatingEventRegistrar<AppPackage> onWindowChange;

	public ActivityMonitor() {
		onWindowChange = new UpdatingEventRegistrar<>();
	}

	public EventRegistrar<AppPackage> onChange() { return onWindowChange; }

	public void reportChange(AppPackage window)	{
		mostRecentWindows.add(window);
		onWindowChange.report(window);
	}

	public Iterable<AppPackage> mostRecentWindows() { return mostRecentWindows; }

}
