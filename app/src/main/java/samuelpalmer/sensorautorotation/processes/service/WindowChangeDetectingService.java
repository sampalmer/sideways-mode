package samuelpalmer.sensorautorotation.processes.service;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

public class WindowChangeDetectingService
		extends AccessibilityService {

	//TODO: Update StackOverflow post with null checks for component name

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (ServiceHolder.isRunning() && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			ActivityInfo activity = getActivity(event);
			if (activity != null)
				ServiceHolder.instance().activityMonitor.setCurrentApp(activity);
		}
	}

	private ActivityInfo getActivity(AccessibilityEvent event) {
		ComponentName activityComponentName = buildComponentName(event);

		if (activityComponentName != null)
			//I'm using exceptions for control flow here because I didn't find a faster approach
			try {
				return getPackageManager().getActivityInfo(activityComponentName, 0);
			} catch (PackageManager.NameNotFoundException ignored) {
			}

		return null;
	}

	private ComponentName buildComponentName(AccessibilityEvent event) {
		if (event.getPackageName() == null || event.getClassName() == null)
			return null;

		return new ComponentName(
			event.getPackageName().toString(),
			event.getClassName().toString()
		);
	}

	@Override
	public void onInterrupt() {}

}