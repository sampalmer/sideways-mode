package samuelpalmer.sensorautorotation.orientationenforcement;

import android.content.Context;

import java.util.Collections;
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

		lockScreenDetector.possiblyChanged().subscribe(lockStatusChangedSubscriber);
		lockScreenDetector.ensureStarted();

		activityMonitor.onChange().subscribe(currentApplicationChangedSubscriber);

		updateEnforcement();
	}

	@Override
	protected void stop() {
		activityMonitor.onChange().unsubscribe(currentApplicationChangedSubscriber);

		lockScreenDetector.ensureStopped();
		lockScreenDetector.possiblyChanged().unsubscribe(lockStatusChangedSubscriber);

		orientationEnforcer.ensureStopped();
	}

	private void updateEnforcement() {
		if (isEnforced() && !LockScreenStatus.isActive(context))
			orientationEnforcer.enforcer.ensureStarted();
		else
			orientationEnforcer.enforcer.ensureStopped();
	}

	private boolean isEnforced() {
		return !Collections.disjoint(orientationEnforcedApps(), activityMonitor.current());
	}

	private Set<String> orientationEnforcedApps() {
		return forcedOrientationAppPackagesSetting.get();
	}

	private final EventHandler<Void> lockStatusChangedSubscriber = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			updateEnforcement();
		}
	};

	private final EventHandler<Void> currentApplicationChangedSubscriber = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
//			Toast.makeText(context, new ArrayList<>(activityMonitor.current()).get(0), Toast.LENGTH_SHORT).show();

			updateEnforcement();
		}
	};

}
