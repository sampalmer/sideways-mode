package samuelpalmer.sensorautorotation.processes.service.orientationenforcement;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.Set;

import samuelpalmer.sensorautorotation.processes.service.PermissionNotificationManager;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.utilities.LockScreenMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;

/**
 * Represents the "overridden apps" feature. Dynamically overrides the screen orientation
 * depending on the currently open activity.
 */
public class OrientationEnforcementMonitor extends State {

	private final ActivityMonitor activityMonitor;
	private final PermissionNotificationManager permissionNotificationManager;
	private final ScreenOrientationEnforcer orientationEnforcer;
	private final LockScreenMonitor lockScreenDetector;
	private final SharedPreferences prefs;
	private final Context context;
	private Set<String> orientationEnforcedPackages;
	
	public OrientationEnforcementMonitor(Context context, ActivityMonitor activityMonitor, PermissionNotificationManager permissionNotificationManager) {
		this.context = context;
		this.activityMonitor = activityMonitor;
		this.permissionNotificationManager = permissionNotificationManager;
		orientationEnforcer = new ScreenOrientationEnforcer(this.context);
		this.lockScreenDetector = new LockScreenMonitor(this.context);
		prefs = ServiceSettings.getSharedPreferences(this.context);
	}

	@Override
	protected void start() {
		lockScreenDetector.subscribe(enforcementUpdater);
		activityMonitor.onChange.subscribe(enforcementUpdater);
		prefs.registerOnSharedPreferenceChangeListener(forcedAppsChanged);

		updatePackages();
		tryToStartEnforcer();
	}

	private void tryToStartEnforcer() {
		if (ScreenOrientationEnforcer.hasPermissionToStart(context)) {
			orientationEnforcer.ensureStarted();
			updateEnforcement();
		}
	}

	@Override
	protected void stop() {
		prefs.unregisterOnSharedPreferenceChangeListener(forcedAppsChanged);
		activityMonitor.onChange.unsubscribe(enforcementUpdater);
		lockScreenDetector.unsubscribe(enforcementUpdater);
		orientationEnforcer.ensureStopped();
	}

	private final SharedPreferences.OnSharedPreferenceChangeListener forcedAppsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {

		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			if (started() && s.equals(ServiceSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES)) {
				updatePackages();
				updateEnforcement();
			}
		}
		
	};

	private void updatePackages() {
		orientationEnforcedPackages = prefs.getStringSet(ServiceSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES, Collections.<String>emptySet());
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
