package samuelpalmer.sensorautorotation.processes.service.screen;

import android.util.Log;

import samuelpalmer.common.Orientation;
import samuelpalmer.sensorautorotation.BuildConfig;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.processes.service.ApplicationState;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ScreenOrientationController extends State {

	private final SystemSettings settings;
	private final ScreenRotationMonitor screen;
	private final ApplicationState state;
	
	public ScreenOrientationController(SystemSettings settings, ScreenRotationMonitor screen, ApplicationState state) {
		this.settings = settings;
		this.screen = screen;
		this.state = state;
	}

	@Override
	protected void start() {
		if (settings.accelerometerRotation.get()) {
			Orientation realOrientation = screen.currentRotation();
			propose(realOrientation);

			settings.accelerometerRotation.set(false);
			propose(realOrientation); //Just in case disabling system rotation reset this.
		}
	}

	@Override
	protected void stop() {
		settings.accelerometerRotation.set(state.autoRotate.get());
		propose(Orientation.NATURAL);
	}

	public void propose(Orientation value) {
		if (value == null)
			throw new IllegalArgumentException();

		if (BuildConfig.DEBUG)
			Log.d(getClass().getSimpleName(), "Proposing " + value);

		settings.userRotation.set(value);
	}

}
