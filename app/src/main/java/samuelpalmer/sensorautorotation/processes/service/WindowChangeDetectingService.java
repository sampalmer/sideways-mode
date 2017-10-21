package samuelpalmer.sensorautorotation.processes.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.utilities.ComponentNameExtensions;

// Keep in mind that renaming (or moving?) this class will result in users having to re-enable the accessibility service.
public class WindowChangeDetectingService extends AccessibilityService {

	private static final String TAG = WindowChangeDetectingService.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();

		ServiceHolder.onChange.subscribe(applicationChanged);

		Log.i(TAG, "Accessibility service created");
	}

	//So far, this only gets called when the user disables the service in the accessibility settings
	//Stopping the service with stopService() doesn't trigger this, presumably because something has bound to it
	@Override
	public void onDestroy() {
		super.onDestroy();

		ServiceHolder.onChange.unsubscribe(applicationChanged);

		Log.i(TAG, "Accessibility service destroyed");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "Accessibility service start command received");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	protected void onServiceConnected() {
		super.onServiceConnected();
		updateServiceInfo();
		Log.i(TAG, "Accessibility service connected");
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (ServiceHolder.isServiceRunning())
			ServiceHolder.getServiceInstance().activityMonitor.setCurrentActivity(null);

		Log.i(TAG, "Accessibility service disconnected");
		return super.onUnbind(intent);
	}

	private final EventHandler applicationChanged = new EventHandler() {
		@Override
		public void update() {
			updateServiceInfo();
		}
	};

	/**
	 * Prevents accessibility events from being received when the application isn't running.
	 * This is an attempt to reduce unnecessary performance impact.
	 */
	private void updateServiceInfo() {
		AccessibilityServiceInfo config = new AccessibilityServiceInfo();

		if (ServiceHolder.isServiceRunning()) {
			config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
			config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

			if (Build.VERSION.SDK_INT >= 16)
				//Just in case this helps
				config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
		}

		setServiceInfo(config);

		Log.i(TAG, "Accessibility service " + ((config.eventTypes == 0) ? "un" : "") + "subscribed for events");
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (ServiceHolder.isServiceRunning() && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			ComponentName componentName = ComponentNameExtensions.tryMakeFrom(event);
			if (componentName != null && isActivity(componentName))
				ServiceHolder.getServiceInstance().activityMonitor.setCurrentActivity(componentName);
		}
	}

	private boolean isActivity(ComponentName componentName) {
		//I'm using exceptions for control flow here because I didn't find a faster approach
		try {
			//It'd be nice to optimise this away, but the only other option I can think of is caching the results, which could waste RAM...
			getPackageManager().getActivityInfo(componentName, 0);
			return true;
		} catch (PackageManager.NameNotFoundException ignored) {
			return false;
		}
	}

	@Override
	public void onInterrupt() {}

}