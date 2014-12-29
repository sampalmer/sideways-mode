package samuelpalmer.sensorautorotation;

import android.content.Context;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.unused.ScreenOrientationEnforcer;

public class OrientationEnforcementMonitor extends State {

	private final Context context;
	private final WindowMonitor monitor;
	private final ScreenOrientationEnforcer orientationEnforcer;

	public OrientationEnforcementMonitor(Context context, WindowMonitor monitor) {
		this.context = context;
		this.monitor = monitor;
		orientationEnforcer = new ScreenOrientationEnforcer(context);
	}

	@Override
	protected void start() throws EnvironmentException {
		monitor.onWindowChange().subscribe(subscriber);
	}

	@Override
	protected void stop() throws EnvironmentException {
		monitor.onWindowChange().unsubscribe(subscriber);
		orientationEnforcer.ensureStopped();
	}

	private final EventHandler<AppPackage> subscriber = new EventHandler<AppPackage>() {
		@Override
		public void update(AppPackage activeApp) throws EnvironmentException {

			if (new Settings(context).enforceOrientation().getOr(false)) {
				Set<String> unsupportedApps = new Settings(context).appPackagesNotSupportingForcedOrientation().getOr(new HashSet<String>(0));
				for (String unsupportedAppName : unsupportedApps) {
					AppPackage unsupportedApp = new AppPackage(unsupportedAppName);
					if (unsupportedApp.equals(activeApp)) {
						orientationEnforcer.ensureStopped();
						return;
					}
				}

				orientationEnforcer.ensureStarted();
			}
			else
				orientationEnforcer.ensureStopped();

		}
	};
}
