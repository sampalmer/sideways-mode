package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class OrientationEnforcementMonitor extends State {

	private final ApplicationSettings settings;
	private final ScreenOrientationEnforcer orientationEnforcer;

	public OrientationEnforcementMonitor(Context context, ApplicationSettings settings) {
		this.settings = settings;
		orientationEnforcer = new ScreenOrientationEnforcer(context);
	}

	@Override
	protected void start() {
	}

	@Override
	protected void stop() {
		orientationEnforcer.ensureStopped();
	}

	public void updateEnforcement(AppPackage activeApp) {
		if (isEnforced(activeApp))
			orientationEnforcer.ensureStarted();
		else
			orientationEnforcer.ensureStopped();
	}

	private boolean isEnforced(AppPackage activeApp) {
		return orientationEnforcedApps().contains(activeApp);
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
