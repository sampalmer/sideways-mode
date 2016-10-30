package samuelpalmer.sensorautorotation.processes.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;

public class WindowChangeDetectingService
		extends AccessibilityService {

	//TODO: Update StackOverflow post with null checks for component name

	/**
	 * Repeats the accessibility service configuration. This is done for two reasons:
	 * 1. Works around a bug in Android 4.1.1 that causes onAccessibilityEvent
	 * to never be called when the accessibility service is enabled for the first time.
	 * (see http://stackoverflow.com/questions/3873659/android-how-can-i-get-the-current-foreground-activity-from-a-service#comment49625193_27642535)
	 * 2. Compatibility with API levels 13 and lower.
	 */
	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();

		AccessibilityServiceInfo config = new AccessibilityServiceInfo();
		config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
		config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

		if (Build.VERSION.SDK_INT >= 16)
			//Just in case this helps
			config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

		setServiceInfo(config);
	}

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