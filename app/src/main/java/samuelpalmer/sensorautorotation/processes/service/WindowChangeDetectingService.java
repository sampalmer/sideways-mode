package samuelpalmer.sensorautorotation.processes.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import samuelpalmer.sensorautorotation.BuildConfig;
import samuelpalmer.sensorautorotation.utilities.ComponentNameExtensions;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

// Keep in mind that renaming (or moving?) this class will result in users having to re-enable the accessibility service.
public class WindowChangeDetectingService extends AccessibilityService {

	private static final String TAG = WindowChangeDetectingService.class.getSimpleName();
	private boolean wasListening = false;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		ServiceRunningStateHolder.onStartedOrStopped.subscribe(applicationChanged);

		Log.i(TAG, "Accessibility service created");
	}

	//So far, this only gets called when the user disables the service in the accessibility settings.
	//Stopping the service with stopService() doesn't trigger this, presumably because something has bound to it.
	@Override
	public void onDestroy() {
		super.onDestroy();
		
		ServiceRunningStateHolder.onStartedOrStopped.unsubscribe(applicationChanged);

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
		Log.i(TAG, "Accessibility service connected");
		updateServiceInfo();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		if (ServiceRunningStateHolder.isServiceRunning())
			ServiceRunningStateHolder.getServiceInstance().activityMonitor.setCurrentActivity(null);

		Log.i(TAG, "Accessibility service disconnected");
		return super.onUnbind(intent);
	}

	private final EventHandler applicationChanged = this::updateServiceInfo;

	/**
	 * Prevents accessibility events from being received when the application isn't running.
	 * This is an attempt to reduce unnecessary performance impact.
	 */
	private void updateServiceInfo() {
		AccessibilityServiceInfo config = new AccessibilityServiceInfo();

		if (ServiceRunningStateHolder.isServiceRunning()) {
			config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
			config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

			if (Build.VERSION.SDK_INT >= 16)
				//Just in case this helps
				config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
		}
		
		setServiceInfo(config);
		Log.i(TAG, "Accessibility service " + ((config.eventTypes == 0) ? "un" : "") + "subscribed for events");
		
		if (ServiceRunningStateHolder.isServiceRunning() && !wasListening) {
			// If we're just starting up, we'll launch a self-closing activity so we can detect which activity the user initially has open.
			Intent intent = new Intent(this, WaitForActivityDetectionActivity.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // <!-- DO NOT use FLAG_ACTIVITY_NO_ANIMATION, since that causes a black flicker when the activity starts or finishes on API 27. -->
			startActivity(intent);
		}
		
		wasListening = ServiceRunningStateHolder.isServiceRunning();
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (ServiceRunningStateHolder.isServiceRunning() && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			if (BuildConfig.DEBUG)
				// This can be useful for development, but we we skip this in Production since it could be a privacy violation.
				Log.d(getClass().getSimpleName(), "Window state changed: " + event.getPackageName() + "/" + event.getClassName());
			
			ComponentName componentName = ComponentNameExtensions.tryMakeFrom(event);
			if (componentName != null && isActivity(componentName))
				ServiceRunningStateHolder.getServiceInstance().activityMonitor.setCurrentActivity(componentName);
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