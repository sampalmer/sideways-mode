package samuelpalmer.sensorautorotation.processes.service.screen;

import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ScreenOrientationController extends State {

	private final SystemSettings settings;
	private final ScreenRotationMonitor screen;
	private final OrientationCalculator calculator;

	public ScreenOrientationController(SystemSettings settings, ScreenRotationMonitor screen, OrientationCalculator calculator) {
		this.settings = settings;
		this.screen = screen;
		this.calculator = calculator;
	}

	@Override
	protected void start() {
		settings.accelerometerRotation.onChange().subscribe(systemAutoRotationChanged);

		Orientation realOrientation = screen.currentRotation();
		propose(realOrientation);

		settings.accelerometerRotation.set(false);
		propose(realOrientation); //Just in case disabling system rotation reset this.
	}

	@Override
	protected void stop() {
		settings.accelerometerRotation.onChange().unsubscribe(systemAutoRotationChanged);

		settings.accelerometerRotation.set(calculator.autoRotate());
		propose(Orientation.NATURAL);
	}

	public void propose(Orientation value) {
		if (value == null)
			throw new IllegalArgumentException();

		settings.userRotation.set(value);
	}

	private final EventHandler systemAutoRotationChanged = new EventHandler() {
		@Override
		public void update() {
			checkIfSystemAutoRotationWasEnabled();
		}
	};

	private void checkIfSystemAutoRotationWasEnabled() {
		boolean accelerometerRotation = settings.accelerometerRotation.get();
		if (accelerometerRotation) {
			//So when the service is stopped, the new auto rotation setting will be reapplied to the system. (Otherwise the user's change to system auto rotation could be reverted.)
			calculator.setAutoRotate(true);
			EnvironmentError.report(R.string.system_rotation_enabled_summary, R.string.system_rotation_enabled_message);
		}
	}

}
