package samuelpalmer.sensorautorotation.processes.service.orientationenforcement;

import java.util.Set;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.processes.service.PermissionNotificationManager;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.settings.application.Settings;

/**
 * Turns the screen orientation enforcement feature on and off as needed.
 *
 * The enforcement feature isn't kept on all the time since:
 * - This would result in unnecessary work
 * - If the feature causes a problem, such as a crash, the user might be able to observe that the problem only started when they enabled this feature.
 */
public class OrientationEnforcementManager extends State {

	private final OrientationEnforcementMonitor monitor;
	private final Settings.Setting<Set<String>> forcedApps;
	private final PermissionNotificationManager permissionNotificationManager;

	public OrientationEnforcementManager(ActivityMonitor activityMonitor) {
		permissionNotificationManager = new PermissionNotificationManager();
		monitor = new OrientationEnforcementMonitor(activityMonitor, permissionNotificationManager);
		forcedApps = ServiceSettings.instance().forcedOrientationAppPackages();
	}

	@Override
	protected void start() {
		permissionNotificationManager.ensureStarted();
		forcedApps.subscribe(forcedAppsChanged);
		updateMonitorState();
	}

	@Override
	protected void stop() {
		forcedApps.unsubscribe(forcedAppsChanged);
		monitor.ensureStopped();
		permissionNotificationManager.ensureStopped();
	}

	private final EventHandler forcedAppsChanged = new EventHandler() {
		@Override
		public void update() {
			updateMonitorState();
		}
	};

	private void updateMonitorState() {
		if (!forcedApps.get().isEmpty())
			monitor.ensureStarted();
		else
			monitor.ensureStopped();
	}

}
