package samuelpalmer.sensorautorotation.components;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.pm.PackageManager;
import android.view.accessibility.AccessibilityEvent;

import java.util.Set;

import samuelpalmer.sensorautorotation.LockScreenDetector;
import samuelpalmer.sensorautorotation.LockScreenStatus;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.orientationenforcement.ScreenOrientationEnforcementManager;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class WindowChangeDetectingService
		extends AccessibilityService {

	private ScreenOrientationEnforcementManager orientationEnforcer;
	private LockScreenDetector lockScreenDetector;
	private ApplicationSettings.Setting<Set<String>> forcedOrientationAppPackagesSetting;
	private String lastApp;

	@Override
	public void onCreate() {
		super.onCreate();
		lastApp = "";

		orientationEnforcer = new ScreenOrientationEnforcementManager(this);
		lockScreenDetector = new LockScreenDetector(this);
		forcedOrientationAppPackagesSetting = new ApplicationSettings(this).forcedOrientationAppPackages();

		orientationEnforcer.ensureStarted();

		lockScreenDetector.possiblyChanged().subscribe(lockStatusChangedSubscriber);
		lockScreenDetector.ensureStarted();

		updateEnforcement();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		lockScreenDetector.ensureStopped();
		lockScreenDetector.possiblyChanged().unsubscribe(lockStatusChangedSubscriber);

		orientationEnforcer.ensureStopped();
	}

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && isForActivity(event)) {
			String currentApp = event.getPackageName().toString();
			if (!currentApp.equals(lastApp)) {
				lastApp = currentApp;
				updateEnforcement();
			}
		}
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


	private void updateEnforcement() {
		if (isEnforced() && !LockScreenStatus.isActive(this))
			orientationEnforcer.enforcer.ensureStarted();
		else
			orientationEnforcer.enforcer.ensureStopped();
	}

	private boolean isEnforced() {
		return orientationEnforcedApps().contains(lastApp);
	}

	private Set<String> orientationEnforcedApps() {
		return forcedOrientationAppPackagesSetting.get();
	}

	private final EventHandler<Void> lockStatusChangedSubscriber = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			updateEnforcement();
		}
	};

}