package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.deviceorientation.OrientationSensorHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class OrientationMessenger extends State {

	private final OrientationSensorHandler orientationListener;

	public OrientationMessenger(Context context) {
		orientationListener = new OrientationSensorHandler(context);
	}

	@Override
	protected void start() {
		orientationListener.ensureStarted();
		orientationListener.onRotationChanged().subscribe(deviceOrientationSubscriber);
	}

	@Override
	protected void stop() {
		orientationListener.onRotationChanged().unsubscribe(deviceOrientationSubscriber);
		orientationListener.ensureStopped();
	}

	final EventHandler<Orientation> deviceOrientationSubscriber = new EventHandler<Orientation>() {

		@Override
		public void update(Orientation event) {
			RotationService.calculator.setDevice(event);
		}

	};

}
