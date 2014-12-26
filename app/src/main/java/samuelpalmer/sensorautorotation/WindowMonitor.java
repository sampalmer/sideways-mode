package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class WindowMonitor {

	private RecentWindows mostRecentWindows = new RecentWindows();
	private final UpdatingEventRegistrar<AppPackage> onWindowChange;

	public WindowMonitor(Context context) {
		onWindowChange = new UpdatingEventRegistrar<>(new UserErrorHandler(context));
	}

	public EventRegistrar<AppPackage> onWindowChange() { return onWindowChange; }

	public void reportWindowChange(AppPackage window)
	{
		mostRecentWindows.add(window);
		onWindowChange.report(window);
	}

	public Iterable<AppPackage> mostRecentWindows() { return mostRecentWindows; }

}
