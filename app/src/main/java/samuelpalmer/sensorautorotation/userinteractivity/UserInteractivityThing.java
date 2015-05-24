package samuelpalmer.sensorautorotation.userinteractivity;

import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.OrientationMessenger;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.StateMonitor;
import samuelpalmer.sensorautorotation.screen.ScreenOrientationUpdater;

public class UserInteractivityThing extends State {
	private final OrientationCalculator calculator;
	private final ScreenOrientationUpdater screenUpdater;
	private final OrientationMessenger orientationMessenger;
	private final UserInteractivityMonitor userInteractivityMonitor;

	public UserInteractivityThing(OrientationCalculator calculator, ScreenOrientationUpdater screenUpdater) {
		this.calculator = calculator;
		this.screenUpdater = screenUpdater;
		orientationMessenger = new OrientationMessenger(calculator);
		userInteractivityMonitor = new UserInteractivityMonitor(listener);
	}

	@Override
	protected void start() {
		userInteractivityMonitor.ensureStarted();
	}

	@Override
	protected void stop() {
		orientationMessenger.ensureStopped();
		screenUpdater.ensureStopped();
		userInteractivityMonitor.ensureStopped();
	}

	private final StateMonitor.Listener<UserInteractivity> listener = new StateMonitor.Listener<UserInteractivity>() {
		@Override
		public void stateChanged(UserInteractivity user) {
			if (user.active()) {
				screenUpdater.ensureStarted();
				orientationMessenger.ensureStarted();
			} else {
				//This should happen before stopping the below things since they need to be triggered by this.
				calculator.reset();

				screenUpdater.ensureStopped();
				orientationMessenger.ensureStopped();
			}
		}
	};
}
