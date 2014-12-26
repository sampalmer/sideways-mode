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
		public void update(AppPackage event) throws EnvironmentException {

			Set<String> enforcedAppPackageNames = new Settings(context).orientationEnforcedAppPackages().getOr(new HashSet<String>(0));
			for (String name : enforcedAppPackageNames)
				if (new AppPackage(name).equals(event)) {
					orientationEnforcer.ensureStarted();
					return;
				}

			orientationEnforcer.ensureStopped();

		}
	};
}
