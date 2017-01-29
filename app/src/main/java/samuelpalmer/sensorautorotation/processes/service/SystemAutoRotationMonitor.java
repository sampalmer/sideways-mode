package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class SystemAutoRotationMonitor extends State {

	private final SystemSettings settings;
	private final OrientationCalculator calculator;

	public SystemAutoRotationMonitor(SystemSettings settings, OrientationCalculator calculator) {
		this.settings = settings;
		this.calculator = calculator;
	}

	@Override
	protected void start() {
		settings.accelerometerRotation.onChange().subscribe(systemAutoRotationChanged);
	}

	@Override
	protected void stop() {
		settings.accelerometerRotation.onChange().unsubscribe(systemAutoRotationChanged);
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
			new EnvironmentError(R.string.environment_error_system_rotation_enabled_summary, R.string.environment_error_system_rotation_enabled_message).report();
		}
	}

}
