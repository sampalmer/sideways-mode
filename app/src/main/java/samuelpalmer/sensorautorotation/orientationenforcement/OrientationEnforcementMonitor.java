package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.LockScreenDetector;
import samuelpalmer.sensorautorotation.LockScreenStatus;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class OrientationEnforcementMonitor extends State {

	private final Context context;
	private final ApplicationSettings settings;
	private final ScreenOrientationEnforcer orientationEnforcer;
	private final LockScreenDetector lockScreenDetector;
	private AppPackage currentApp;

	public OrientationEnforcementMonitor(Context context, ApplicationSettings settings) {
		this.context = context;
		this.settings = settings;
		orientationEnforcer = new ScreenOrientationEnforcer(context);
		lockScreenDetector = new LockScreenDetector(context);
	}

	@Override
	protected void start() {
		currentApp = new AppPackage("");
		lockScreenDetector.possiblyChanged().subscribe(subscriber);
		lockScreenDetector.ensureStarted();
	}

	@Override
	protected void stop() {
		lockScreenDetector.ensureStopped();
		lockScreenDetector.possiblyChanged().unsubscribe(subscriber);
		orientationEnforcer.ensureStopped();
	}

	public void reportApp(AppPackage activeApp) {
		currentApp = activeApp;
		updateEnforcement();
	}

	private void updateEnforcement() {
		if (isEnforced() && !LockScreenStatus.isActive(context))
			orientationEnforcer.ensureStarted();
		else
			orientationEnforcer.ensureStopped();
	}

	private boolean isEnforced() {
		return orientationEnforcedApps().contains(currentApp);
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

	private EventHandler<Void> subscriber = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			updateEnforcement();
		}
	};

}
