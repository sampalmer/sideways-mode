package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class OrientationEnforcementMonitor extends State {

	private final ActivityMonitor monitor;
	private final ApplicationSettings settings;
	private final ScreenOrientationEnforcer orientationEnforcer;

	public OrientationEnforcementMonitor(Context context, ActivityMonitor monitor, ApplicationSettings settings) {
		this.monitor = monitor;
		this.settings = settings;
		orientationEnforcer = new ScreenOrientationEnforcer(context);
	}

	@Override
	protected void start() {
		monitor.onChange().subscribe(subscriber);
	}

	@Override
	protected void stop() {
		monitor.onChange().unsubscribe(subscriber);
		orientationEnforcer.ensureStopped();
	}

	private final EventHandler<Set<AppPackage>> subscriber = new EventHandler<Set<AppPackage>>() {
		@Override
		public void update(Set<AppPackage> activeApps) {
			updateEnforcement(activeApps);
		}
	};

	private void updateEnforcement(Set<AppPackage> activeApps) {
		if (isEnforced(activeApps))
			orientationEnforcer.ensureStarted();
		else
			orientationEnforcer.ensureStopped();
	}

	private boolean isEnforced(Set<AppPackage> activeApps) {
		HashSet<AppPackage> activeOrientationEnforcedApps = new HashSet<>(orientationEnforcedApps());
		activeOrientationEnforcedApps.retainAll(activeApps);

		return !activeOrientationEnforcedApps.isEmpty();
	}

	private Set<AppPackage> orientationEnforcedApps() {
		Set<String> orientationEnforcedAppsPackagesNames = settings.forcedOrientationAppPackages().get();
		Set<AppPackage> orientationEnforcedAppPackages = new HashSet<>(orientationEnforcedAppsPackagesNames.size());
		for (String orientationEnforcedAppPackage : orientationEnforcedAppsPackagesNames) {
			AppPackage orientationEnforcedApp = new AppPackage(orientationEnforcedAppPackage);
			orientationEnforcedAppPackages.add(orientationEnforcedApp);
		}

		return orientationEnforcedAppPackages;
	}
}
