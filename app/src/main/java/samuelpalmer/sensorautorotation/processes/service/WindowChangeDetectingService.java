package samuelpalmer.sensorautorotation.processes.service;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

public class WindowChangeDetectingService extends AccessibilityService {

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (ServiceHolder.isRunning() && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			ActivityInfo activity = tryGetActivity(event);
			if (activity != null)
				ServiceHolder.instance().activityMonitor.setCurrentApp(activity);
		}
	}

	private ActivityInfo tryGetActivity(AccessibilityEvent event) {
		if (event.getPackageName() != null && event.getClassName() != null) {
			ComponentName componentName = new ComponentName(
				event.getPackageName().toString(),
				event.getClassName().toString()
			);

			//I'm using exceptions for control flow here because I didn't find a faster approach
			try {
				return getPackageManager().getActivityInfo(componentName, 0);
			} catch (PackageManager.NameNotFoundException ignored) {}
		}

		return null;
	}

	@Override
	public void onInterrupt() {}

}