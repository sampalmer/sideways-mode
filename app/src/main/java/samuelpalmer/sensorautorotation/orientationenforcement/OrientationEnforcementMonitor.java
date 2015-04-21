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
	private final ScreenOrientationEnforcer orientationEnforcer;
	private final LockScreenDetector lockScreenDetector;
	private final ApplicationSettings.Setting<Set<String>> forcedOrientationAppPackagesSetting;
	private Set<String> currentAppPackages;

	public OrientationEnforcementMonitor(Context context, ApplicationSettings settings, ActivityMonitor activityMonitor) {
		this.context = context;
		this.activityMonitor = activityMonitor;
		orientationEnforcer = new ScreenOrientationEnforcer(context);
		lockScreenDetector = new LockScreenDetector(context);
		forcedOrientationAppPackagesSetting = settings.forcedOrientationAppPackages();
	}

	@Override
	protected void start() {
		currentAppPackages = Collections.emptySet();

		lockScreenDetector.possiblyChanged().subscribe(lockStatusChangedSubscriber);
		lockScreenDetector.ensureStarted();

		activityMonitor.onChange().subscribe(currentApplicationChangedSubscriber);
	}

	@Override
	protected void stop() {
		activityMonitor.onChange().subscribe(currentApplicationChangedSubscriber);

		lockScreenDetector.ensureStopped();
		lockScreenDetector.possiblyChanged().unsubscribe(lockStatusChangedSubscriber);

		orientationEnforcer.ensureStopped();
	}

	private void updateEnforcement() {
		if (isEnforced() && !LockScreenStatus.isActive(context))
			orientationEnforcer.ensureStarted();
		else
			orientationEnforcer.ensureStopped();
	}

	private boolean isEnforced() {
		return !Collections.disjoint(orientationEnforcedApps(), currentAppPackages);
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

	private final EventHandler<Set<String>> currentApplicationChangedSubscriber = new EventHandler<Set<String>>() {
		@Override
		public void update(Set<String> event) {
			currentAppPackages = event;
			updateEnforcement();
		}
	};

}
