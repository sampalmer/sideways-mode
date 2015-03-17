package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class UserInteractivityThing extends State {
	private final OrientationCalculator calculator;
	private final ScreenOrientationUpdater screenUpdater;
	private final OrientationMessenger orientationMessenger;
	private final UserInteractivityMonitor userInteractivityMonitor;

	public UserInteractivityThing(Context context, ApplicationSettings appSettings, OrientationCalculator calculator, ScreenOrientationController screenOrientationController) {
		this.calculator = calculator;
		screenUpdater = new ScreenOrientationUpdater(appSettings, calculator, screenOrientationController);
		orientationMessenger = new OrientationMessenger(context, calculator);
		userInteractivityMonitor = new UserInteractivityMonitor(context, listener);
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
				screenUpdater.ensureStopped();
				orientationMessenger.ensureStopped();

				calculator.reset();
			}
		}
	};
}
