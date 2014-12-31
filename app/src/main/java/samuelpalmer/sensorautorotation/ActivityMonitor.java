package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ActivityMonitor {

	private RecentWindows mostRecentWindows = new RecentWindows();
	private final UpdatingEventRegistrar<AppPackage> onWindowChange;

	public ActivityMonitor(Context context) {
		onWindowChange = new UpdatingEventRegistrar<>(new UserErrorHandler(context));
	}

	public EventRegistrar<AppPackage> onChange() { return onWindowChange; }

	public void reportChange(AppPackage window)	{
		mostRecentWindows.add(window);
		onWindowChange.report(window);
	}

	public Iterable<AppPackage> mostRecentWindows() { return mostRecentWindows; }

}
