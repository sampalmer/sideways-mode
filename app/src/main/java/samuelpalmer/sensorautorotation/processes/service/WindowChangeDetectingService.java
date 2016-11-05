package samuelpalmer.sensorautorotation.processes.service;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class WindowChangeDetectingService extends AccessibilityService {

	private final String TAG = WindowChangeDetectingService.class.getSimpleName();

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "Accessibility service created");

		//TODO: Warn the user to disable this service if the app isn't enabled
		//TODO: Unregister for accessibility events if the app isn't enabled (setServiceInfo(new AccessibilityServiceInfo()))
	}

	//So far, this only gets called when the user disables the service in the accessibility settings
	//Stopping the service doesn't trigger this, presumably because something has bound to it
	@Override
	public void onDestroy() {
		super.onDestroy();
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
	}

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
			//Might be able to improve performance by removing the activity check and just passing the component name straight to the app.
			try {
				return getPackageManager().getActivityInfo(componentName, 0);
			} catch (PackageManager.NameNotFoundException ignored) {}
		}

		return null;
	}

	@Override
	public void onInterrupt() {}

}