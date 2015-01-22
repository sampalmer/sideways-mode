package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.components.RotationService;

public class UserActiveState extends State {

	private OrientationMessenger orientationMessenger;
	private ScreenOrientationUpdater screenUpdater;

	public UserActiveState(Context context) {
		screenUpdater = new ScreenOrientationUpdater(context, RotationService.calculator, RotationService.screenOrientationController);
		orientationMessenger = new OrientationMessenger(context);
	}

	@Override
	protected void start() {
		screenUpdater.ensureStarted();
		RotationService.calculator.ensureStarted();
		orientationMessenger.ensureStarted();
	}

	@Override
	protected void stop() {
		orientationMessenger.ensureStopped();
		RotationService.calculator.ensureStopped();
		screenUpdater.ensureStopped();
	}

}
