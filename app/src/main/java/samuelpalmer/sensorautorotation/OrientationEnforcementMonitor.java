package samuelpalmer.sensorautorotation;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.unused.ScreenOrientationEnforcer;

public class OrientationEnforcementMonitor extends State {

	private final Context context;
	private final ActivityMonitor monitor;
	private final ScreenOrientationEnforcer orientationEnforcer;

	public OrientationEnforcementMonitor(Context context, ActivityMonitor monitor) {
		this.context = context;
		this.monitor = monitor;
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

	private final EventHandler<AppPackage> subscriber = new EventHandler<AppPackage>() {
		@Override
		public void update(AppPackage activeApp) {

			Set<String> orientationEnforcedAppsPackages = new Settings(context).forcedOrientationAppPackages().getOr(new HashSet<String>(0));
			for (String orientationEnforcedAppPackage : orientationEnforcedAppsPackages) {
				AppPackage orientationEnforcedApp = new AppPackage(orientationEnforcedAppPackage);
				if (orientationEnforcedApp.equals(activeApp)) {
					orientationEnforcer.ensureStarted();
					return;
				}
			}

			orientationEnforcer.ensureStopped();
		}
	};
}
