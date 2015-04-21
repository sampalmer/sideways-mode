package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.orientationenforcement.ActivityMonitor;

public class UserInteractivityThing extends State {
	private final OrientationCalculator calculator;
	private final ScreenOrientationUpdater screenUpdater;
	private final ActivityMonitor activityMonitor;
	private final OrientationMessenger orientationMessenger;
	private final UserInteractivityMonitor userInteractivityMonitor;

	public UserInteractivityThing(Context context, OrientationCalculator calculator, ScreenOrientationUpdater screenUpdater, ActivityMonitor activityMonitor) {
		this.calculator = calculator;
		this.screenUpdater = screenUpdater;
		this.activityMonitor = activityMonitor;
		orientationMessenger = new OrientationMessenger(context, calculator);
		userInteractivityMonitor = new UserInteractivityMonitor(context, listener);
	}

	@Override
	protected void start() {
		userInteractivityMonitor.ensureStarted();
	}

	@Override
	protected void stop() {
		activityMonitor.stop();
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
				activityMonitor.ensureStarted();
			} else {
				screenUpdater.ensureStopped();
				orientationMessenger.ensureStopped();
				activityMonitor.ensureStopped();

				calculator.reset();
			}
		}
	};
}
