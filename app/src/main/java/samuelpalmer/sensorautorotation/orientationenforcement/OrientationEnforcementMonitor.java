package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;
import android.content.pm.ActivityInfo;

import java.util.Set;

import samuelpalmer.sensorautorotation.LockScreenDetector;
import samuelpalmer.sensorautorotation.LockScreenStatus;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class OrientationEnforcementMonitor extends State {

	private final Context context;
	private final ActivityMonitor activityMonitor;
	private final ScreenOrientationEnforcementManager orientationEnforcer;
	private final LockScreenDetector lockScreenDetector;
	private final ApplicationSettings.Setting<Set<String>> forcedOrientationAppPackagesSetting;

	public OrientationEnforcementMonitor(Context context, ApplicationSettings settings, ActivityMonitor activityMonitor) {
		this.context = context;
		this.activityMonitor = activityMonitor;
		orientationEnforcer = new ScreenOrientationEnforcementManager(context);
		lockScreenDetector = new LockScreenDetector(context);
		forcedOrientationAppPackagesSetting = settings.forcedOrientationAppPackages();
	}

	@Override
	protected void start() {
		orientationEnforcer.ensureStarted();

		lockScreenDetector.possiblyChanged().subscribe(enforcementUpdater);
		lockScreenDetector.ensureStarted();

		activityMonitor.onChange().subscribe(enforcementUpdater);

		updateEnforcement();
	}

	@Override
	protected void stop() {
		activityMonitor.onChange().unsubscribe(enforcementUpdater);

		lockScreenDetector.ensureStopped();
		lockScreenDetector.possiblyChanged().unsubscribe(enforcementUpdater);

		orientationEnforcer.ensureStopped();
	}

	private void updateEnforcement() {
		if (isEnforced() && !LockScreenStatus.isActive(context))
			orientationEnforcer.setOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
		else
			orientationEnforcer.setOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	private boolean isEnforced() {
		return orientationEnforcedPackages().contains(activityMonitor.current().getPackage());
	}

	private Set<String> orientationEnforcedPackages() {
		return forcedOrientationAppPackagesSetting.get();
	}

	private final EventHandler<Void> enforcementUpdater = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			updateEnforcement();
		}
	};

}
