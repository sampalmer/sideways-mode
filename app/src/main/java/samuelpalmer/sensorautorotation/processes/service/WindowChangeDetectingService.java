package samuelpalmer.sensorautorotation.processes.service;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

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

		//TODO: Warn the user to disable the accessibility service to avoid RAM waste. Looks like it's impossible to programmatically start and stop it.
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
				return getPackageManager().getActivityInfo(componentName, 0); //TODO: Optimise this away; only check if it's an activity after first checking its package. This should save an interprocess call.
			} catch (PackageManager.NameNotFoundException ignored) {}
		}

		return null;
	}

	@Override
	public void onInterrupt() {}

}