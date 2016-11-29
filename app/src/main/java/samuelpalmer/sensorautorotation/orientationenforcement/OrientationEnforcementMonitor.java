package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.ComponentName;
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
	private Set<String> orientationEnforcedPackages;

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
		forcedOrientationAppPackagesSetting.subscribe(packagesUpdater);

		updatePackages();
		updateEnforcement();
	}

	@Override
	protected void stop() {
		forcedOrientationAppPackagesSetting.unsubscribe(packagesUpdater);
		activityMonitor.onChange().unsubscribe(enforcementUpdater);
		lockScreenDetector.unsubscribe(enforcementUpdater);
		orientationEnforcer.ensureStopped();
	}

	private EventHandler packagesUpdater = new EventHandler() {
		@Override
		public void update() {
			updatePackages();
			updateEnforcement();
		}
	};

	private void updatePackages() {
		orientationEnforcedPackages = forcedOrientationAppPackagesSetting.get();
	}

	private void updateEnforcement() {
		//Not allowing lock screen orientation enforcement just in case it breaks the lock screen
		boolean shouldEnforce = isEnforced() && !lockScreenDetector.isOnLockScreen();
		int forcedOrientation = shouldEnforce ? ActivityInfo.SCREEN_ORIENTATION_USER : ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
		orientationEnforcer.setOrientation(forcedOrientation);
	}

	private boolean isEnforced() {
		ComponentName currentActivity = activityMonitor.currentOrNull();

		return
			currentActivity != null
			&&
			orientationEnforcedPackages.contains(currentActivity.getPackageName());
	}

	private final EventHandler enforcementUpdater = new EventHandler() {
		@Override
		public void update() {
			updateEnforcement();
		}
	};

}
