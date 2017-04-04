package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.ComponentName;

import java.util.Set;

import samuelpalmer.common.LockScreenMonitor;
import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.processes.service.PermissionNotificationManager;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class OrientationEnforcementMonitor extends State {

	private final ActivityMonitor activityMonitor;
	private final PermissionNotificationManager permissionNotificationManager;
	private final ScreenOrientationEnforcer orientationEnforcer;
	private final LockScreenMonitor lockScreenDetector;
	private final Settings.Setting<Set<String>> forcedOrientationAppPackagesSetting;
	private Set<String> orientationEnforcedPackages;

	public OrientationEnforcementMonitor(ActivityMonitor activityMonitor, LockScreenMonitor lockScreenMonitor, PermissionNotificationManager permissionNotificationManager) {
		this.activityMonitor = activityMonitor;
		this.permissionNotificationManager = permissionNotificationManager;
		orientationEnforcer = new ScreenOrientationEnforcer(CurrentProcess.context());
		this.lockScreenDetector = lockScreenMonitor;
		forcedOrientationAppPackagesSetting = ApplicationSettings.local().forcedOrientationAppPackages();
	}

	@Override
	protected void start() {
		lockScreenDetector.subscribe(enforcementUpdater);
		activityMonitor.onChange().subscribe(enforcementUpdater);
		forcedOrientationAppPackagesSetting.subscribe(forcedAppsChanged);

		updatePackages();
		tryToStartEnforcer();
	}

	private void tryToStartEnforcer() {
		if (ScreenOrientationEnforcer.hasPermissionToStart(CurrentProcess.context())) {
			orientationEnforcer.ensureStarted();
			updateEnforcement();
		}
	}

	@Override
	protected void stop() {
		forcedOrientationAppPackagesSetting.unsubscribe(forcedAppsChanged);
		activityMonitor.onChange().unsubscribe(enforcementUpdater);
		lockScreenDetector.unsubscribe(enforcementUpdater);
		orientationEnforcer.ensureStopped();
	}

	private final EventHandler forcedAppsChanged = new EventHandler() {
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
		if (orientationEnforcer.started()) {
			//Note that the permission may have been revoked at this point, but it doesn't cause a crash. Not checking it here since it could introduce lag.

			//Not allowing lock screen orientation enforcement just in case it breaks the lock screen
			boolean shouldEnforce = isEnforced() && !lockScreenDetector.isOnLockScreen();
			orientationEnforcer.setEnforced(shouldEnforce);
		}
		else {
			tryToStartEnforcer();
			if (orientationEnforcer.started())
				permissionNotificationManager.check(); //Android doesn't tell us when the permission is granted, so we'll just keep checking it so the error notification can be cancelled
		}
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
