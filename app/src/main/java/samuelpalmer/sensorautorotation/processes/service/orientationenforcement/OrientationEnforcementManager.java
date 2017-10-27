package samuelpalmer.sensorautorotation.processes.service.orientationenforcement;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

import samuelpalmer.sensorautorotation.processes.service.PermissionNotificationManager;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.utilities.State;

/**
 * Turns the screen orientation enforcement feature on and off as needed.
 *
 * The enforcement feature isn't kept on all the time since:
 * - This would result in unnecessary work
 * - If the feature causes a problem, such as a crash, the user might be able to observe that the problem only started when they enabled this feature.
 */
public class OrientationEnforcementManager extends State {

	private final OrientationEnforcementMonitor monitor;
	private final SharedPreferences prefs;
	private final PermissionNotificationManager permissionNotificationManager;
	
	public OrientationEnforcementManager(Context context, ActivityMonitor activityMonitor) {
		permissionNotificationManager = new PermissionNotificationManager(context);
		monitor = new OrientationEnforcementMonitor(context, activityMonitor, permissionNotificationManager);
		prefs = ServiceSettings.getSharedPreferences(context);
	}

	@Override
	protected void start() {
		permissionNotificationManager.ensureStarted();
		prefs.registerOnSharedPreferenceChangeListener(forcedAppsChanged);
		
		updateMonitorState();
	}

	@Override
	protected void stop() {
		prefs.unregisterOnSharedPreferenceChangeListener(forcedAppsChanged);
		monitor.ensureStopped();
		permissionNotificationManager.ensureStopped();
	}
	
	// This must be stored in a field to prevent garbage collection.
	private final SharedPreferences.OnSharedPreferenceChangeListener forcedAppsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			if (isStarted() && s.equals(ServiceSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES))
				updateMonitorState();
		}
	};
	
	private void updateMonitorState() {
		Set<String> forcedAppPackages = prefs.getStringSet(ServiceSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES, null);
		
		if (forcedAppPackages != null && !forcedAppPackages.isEmpty())
			monitor.ensureStarted();
		else
			monitor.ensureStopped();
	}

}
