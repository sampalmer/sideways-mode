package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;
import android.content.pm.ActivityInfo;

import java.util.Set;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class OrientationEnforcementMonitor extends State {

	private final ActivityMonitor activityMonitor;
	private final ScreenOrientationEnforcementManager orientationEnforcer;
	private final LockScreenMonitor lockScreenDetector;
	private final Settings.Setting<Set<String>> forcedOrientationAppPackagesSetting;

	public OrientationEnforcementMonitor(Context context, ActivityMonitor activityMonitor, LockScreenMonitor lockScreenMonitor) {
		this.activityMonitor = activityMonitor;
		orientationEnforcer = new ScreenOrientationEnforcementManager(context);
		this.lockScreenDetector = lockScreenMonitor;
		forcedOrientationAppPackagesSetting = ApplicationSettings.local().forcedOrientationAppPackages();
	}

	@Override
	protected void start() {
		orientationEnforcer.ensureStarted();
		lockScreenDetector.subscribe(enforcementUpdater);
		activityMonitor.onChange().subscribe(enforcementUpdater);

		updateEnforcement();
	}

	@Override
	protected void stop() {
		activityMonitor.onChange().unsubscribe(enforcementUpdater);
		lockScreenDetector.unsubscribe(enforcementUpdater);
		orientationEnforcer.ensureStopped();
	}

	private void updateEnforcement() {
		boolean shouldEnforce = isEnforced() && !lockScreenDetector.isOnLockScreen();
		int forcedOrientation = shouldEnforce ? ActivityInfo.SCREEN_ORIENTATION_USER : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
		orientationEnforcer.setOrientation(forcedOrientation);
	}

	private boolean isEnforced() {
		CurrentActivity currentActivity = activityMonitor.currentOrNull();
		return currentActivity != null && orientationEnforcedPackages().contains(currentActivity.getPackage());
	}

	private Set<String> orientationEnforcedPackages() {
		//TODO: Subscribe to the setting and keep a cached version to save RAM and performance impact
		return forcedOrientationAppPackagesSetting.get();
	}

	private final EventHandler enforcementUpdater = new EventHandler() {
		@Override
		public void update() {
			updateEnforcement();
		}
	};

}
