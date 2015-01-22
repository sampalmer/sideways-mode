package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.components.RotationService;

public class UserActiveState extends State {

	private final RotationService service;
	private OrientationMessenger orientationMessenger;
	private ScreenOrientationUpdater screenUpdater;

	public UserActiveState(RotationService context) {
		service = context;
		screenUpdater = new ScreenOrientationUpdater(context, RotationService.calculator, RotationService.screenOrientationController);
		orientationMessenger = new OrientationMessenger(context);
	}

	@Override
	protected void start() {
		screenUpdater.ensureStarted();
		orientationMessenger.ensureStarted();
	}

	@Override
	protected void stop() {
		orientationMessenger.ensureStopped();

		RotationService.calculator.reset();
		service.setRecalibrating(true);

		screenUpdater.ensureStopped();
	}

}
