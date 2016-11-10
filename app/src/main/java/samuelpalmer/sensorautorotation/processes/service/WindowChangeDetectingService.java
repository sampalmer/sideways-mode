package samuelpalmer.sensorautorotation.processes.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

import samuelpalmer.common.ComponentNameExtensions;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class WindowChangeDetectingService extends AccessibilityService {

	private static final String TAG = WindowChangeDetectingService.class.getSimpleName();
	private Settings.Setting<Boolean> enabled;

	@Override
	public void onCreate() {
		super.onCreate();

		enabled = ApplicationSettings.local().enabled();

		ServiceHolder.onChange.subscribe(mainServiceChanged);

		enabled.subscribe(enabledChanged);
		disableIfNecessary();

		Log.i(TAG, "Accessibility service created");
	}

	//So far, this only gets called when the user disables the service in the accessibility settings
	//Stopping the service doesn't trigger this, presumably because something has bound to it
	@Override
	public void onDestroy() {
		super.onDestroy();

		enabled.unsubscribe(enabledChanged);
		ServiceHolder.onChange.unsubscribe(mainServiceChanged);

		enabled = null;

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
		if (ServiceHolder.isRunning())
			ServiceHolder.instance().activityMonitor.setCurrentActivity(null);

		Log.i(TAG, "Accessibility service disconnected");
		return super.onUnbind(intent);
	}

	private final EventHandler mainServiceChanged = new EventHandler() {
		@Override
		public void update() {
			updateServiceInfo();
		}
	};

	/**
	 * Prevents accessibility events from being received when the main service isn't running.
	 * This is an attempt to reduce unnecessary performance impact.
	 */
	private void updateServiceInfo() {
		AccessibilityServiceInfo config = new AccessibilityServiceInfo();

		if (ServiceHolder.isRunning()) {
			config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
			config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

			if (Build.VERSION.SDK_INT >= 16)
				//Just in case this helps
				config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;
		}

		setServiceInfo(config);

		Log.i(TAG, "Accessibility service " + ((config.eventTypes == 0) ? "un" : "") + "subscribed for events");
	}

	private final EventHandler enabledChanged = new EventHandler() {
		@Override
		public void update() {
			disableIfNecessary();
		}
	};

	private void disableIfNecessary() {
		if (Build.VERSION.SDK_INT >= 24 && !enabled.get()) {
			disableSelf();
			Log.i(TAG, "Accessibility service disabled since application is disabled");
		}

		//TODO: Warn the user to disable the accessibility service to avoid RAM waste. Looks like it's impossible to programmatically start and stop it. The warning to disable the accessibility service should provide an ok button to take the user to the accessibility settings.
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		//TODO: DON'T COMMIT THIS
		Log.i(TAG, event.getClassName().toString());

		if (ServiceHolder.isRunning() && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
			ComponentName componentName = ComponentNameExtensions.tryMakeFrom(event);
			if (componentName != null && isActivity(componentName))
					ServiceHolder.instance().activityMonitor.setCurrentActivity(componentName);
		}
	}

	private boolean isActivity(ComponentName componentName) {
		//I'm using exceptions for control flow here because I didn't find a faster approach
		//Might be able to improve performance by removing the activity check and just passing the component name straight to the app.
		try {
			getPackageManager().getActivityInfo(componentName, 0); //TODO: Optimise this away; only check if it's an activity after first checking its package. This should save an interprocess call.
			return true;
		} catch (PackageManager.NameNotFoundException ignored) {
			return false;
		}
	}

	@Override
	public void onInterrupt() {}

}