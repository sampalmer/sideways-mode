package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class UserActiveState extends State {

	private OrientationSensorHandler orientationListener;
	private ScreenOrientationUpdater screenUpdater;

	public UserActiveState(Context context) {
		screenUpdater = new ScreenOrientationUpdater(context, RotationService.calculator, RotationService.screenOrientationController);
		orientationListener = new OrientationSensorHandler(context);
	}

	@Override
	protected void start() {
		screenUpdater.ensureStarted();
		RotationService.calculator.ensureStarted();
		orientationListener.ensureStarted();

		orientationListener.onRotationChanged().subscribe(deviceOrientationSubscriber);
	}

	@Override
	protected void stop() {
		orientationListener.onRotationChanged().unsubscribe(deviceOrientationSubscriber);

		if (orientationListener != null)
			orientationListener.ensureStopped();

		RotationService.calculator.ensureStopped();
		screenUpdater.ensureStopped();
	}

	EventHandler<Orientation> deviceOrientationSubscriber = new EventHandler<Orientation>() {

		@Override
		public void update(Orientation event) {
			RotationService.calculator.setDevice(event);
		}
	};

}
