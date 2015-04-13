package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.orientationenforcement.ActivityMonitor;
import samuelpalmer.sensorautorotation.orientationenforcement.OrientationEnforcementMonitor;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class UserInteractivityThing extends State {
	private final OrientationCalculator calculator;
	private final ScreenOrientationUpdater screenUpdater;
	private final OrientationMessenger orientationMessenger;
	private final UserInteractivityMonitor userInteractivityMonitor;
	private final OrientationEnforcementMonitor orientationEnforcementMonitor;
	private final ActivityMonitor activityMonitor;

	public UserInteractivityThing(Context context, OrientationCalculator calculator, ScreenOrientationUpdater screenUpdater, ApplicationSettings settings) {
		this.calculator = calculator;
		this.screenUpdater = screenUpdater;
		orientationMessenger = new OrientationMessenger(context, calculator);
		userInteractivityMonitor = new UserInteractivityMonitor(context, listener);
		activityMonitor = new ActivityMonitor(context);
		orientationEnforcementMonitor = new OrientationEnforcementMonitor(context, activityMonitor, settings);
	}

	@Override
	protected void start() {
		userInteractivityMonitor.ensureStarted();
		orientationEnforcementMonitor.ensureStarted();
	}

	@Override
	protected void stop() {
		orientationEnforcementMonitor.ensureStopped();
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
