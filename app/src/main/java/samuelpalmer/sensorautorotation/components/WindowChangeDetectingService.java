package samuelpalmer.sensorautorotation.components;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.view.accessibility.AccessibilityEvent;

import samuelpalmer.sensorautorotation.ServiceApiBinder;
import samuelpalmer.sensorautorotation.ServiceBinder;

public class WindowChangeDetectingService
		extends AccessibilityService {

	private ServiceBinder serviceBinder;

	@Override
	public void onCreate() {
		super.onCreate();
		serviceBinder = new ServiceBinder(this, connection);
		serviceBinder.bind();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		serviceBinder.unbind();
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (
			mainService != null
			&&
			event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
			&&
			isForActivity(event)
		) {
			String currentApp = event.getPackageName().toString();
			mainService.updateCurrentApp(currentApp);
		}
	}

	private boolean isForActivity(AccessibilityEvent event) {
		if (event.getPackageName() == null || event.getClassName() == null)
			return false;

		ComponentName activityComponentName = new ComponentName(
				event.getPackageName().toString(),
				event.getClassName().toString());

		return isActivity(activityComponentName);
	}

	private boolean isActivity(ComponentName activityComponentName) {
		//I'm using exceptions for control flow here because I couldn't find a faster approach
		try {
			getPackageManager().getActivityInfo(activityComponentName, 0);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}

		return true;
	}

	@Override
	public void onInterrupt() {}

	private ServiceApiBinder mainService;

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mainService = (ServiceApiBinder)service;
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mainService = null;
			serviceBinder.bind(); //So onServiceConnected will be called when the target service is back
		}
	};

}