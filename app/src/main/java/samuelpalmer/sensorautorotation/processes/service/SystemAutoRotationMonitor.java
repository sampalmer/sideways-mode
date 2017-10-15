package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.errors.GenericError;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class SystemAutoRotationMonitor extends State {

	private final SystemSettings settings;
	
	public SystemAutoRotationMonitor(SystemSettings settings) {
		this.settings = settings;
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
			GenericError error = new GenericError(R.string.environment_error_system_rotation_enabled_summary, CurrentProcess.context().getText(R.string.environment_error_system_rotation_enabled_message));
			EnvironmentErrorManager.report(error);
		}
	}

}
