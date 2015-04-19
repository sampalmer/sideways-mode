package samuelpalmer.sensorautorotation.components;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

import samuelpalmer.sensorautorotation.ServiceApiBinder;
import samuelpalmer.sensorautorotation.ServiceCommandRunner;
import samuelpalmer.sensorautorotation.orientationenforcement.AppPackage;

public class WindowChangeDetectingService
		extends AccessibilityService {

	private AppPackage lastApp;

	@Override
	public void onCreate() {
		super.onCreate();

		lastApp = new AppPackage("");
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && isForActivity(event)) {
			final AppPackage currentApp = new AppPackage(event.getPackageName());
			if (!currentApp.equals(lastApp)) {
				lastApp = currentApp;
				report(currentApp);
			}
		}
	}

	private void report(final AppPackage currentApp) {
		ServiceCommandRunner serviceCommandRunner = new ServiceCommandRunner(this, new ServiceCommandRunner.Command() {
			@Override
			public void run(ServiceApiBinder service) {
				service.reportAppChange(currentApp);
			}
		});

		serviceCommandRunner.run();
	}

	private boolean isForActivity(AccessibilityEvent event) {
		ComponentName activityComponentName = new ComponentName(
				event.getPackageName().toString(),
				event.getClassName().toString());

		try {
			getPackageManager().getActivityInfo(activityComponentName, 0);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}

		return true;
	}

	@Override
	public void onInterrupt() {}
}