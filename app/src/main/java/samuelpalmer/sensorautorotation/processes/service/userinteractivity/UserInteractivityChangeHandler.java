package samuelpalmer.sensorautorotation.processes.service.userinteractivity;

import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.StateMonitor;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
import samuelpalmer.sensorautorotation.processes.service.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenOrientationUpdater;

public class UserInteractivityChangeHandler extends State {
	private final OrientationCalculator calculator;
	private final ScreenOrientationUpdater screenUpdater;
	private final OrientationSensorHandler orientationSensorHandler;
	private final UserInteractivityMonitor userInteractivityMonitor;

	public UserInteractivityChangeHandler(OrientationCalculator calculator, ScreenOrientationUpdater screenUpdater) {
		this.calculator = calculator;
		this.screenUpdater = screenUpdater;
		orientationSensorHandler = new OrientationSensorHandler(calculator);
		userInteractivityMonitor = new UserInteractivityMonitor(listener);
	}

	@Override
	protected void start() {
		userInteractivityMonitor.ensureStarted();
	}

	@Override
	protected void stop() {
		orientationSensorHandler.ensureStopped();
		screenUpdater.ensureStopped();
		userInteractivityMonitor.ensureStopped();
	}

	private final StateMonitor.Listener<UserInteractivity> listener = new StateMonitor.Listener<UserInteractivity>() {
		@Override
		public void stateChanged(UserInteractivity user) {
			if (user.active()) {
				screenUpdater.ensureStarted();
				orientationSensorHandler.ensureStarted();
			} else {
				//This should happen before stopping the below things since they need to be triggered by this.
				calculator.reset();

				screenUpdater.ensureStopped();
				orientationSensorHandler.ensureStopped();
			}
		}
	};
}
