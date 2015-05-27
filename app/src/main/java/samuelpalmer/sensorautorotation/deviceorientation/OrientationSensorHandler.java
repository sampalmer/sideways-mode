package samuelpalmer.sensorautorotation.deviceorientation;

import samuelpalmer.sensorautorotation.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.ThisApplication;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;


public class OrientationSensorHandler extends State {

	private final WindowOrientationListener listener;
	private final UpdatingEventRegistrar<Orientation> subscriptions;
	private Integer lastRotation;

	public OrientationSensorHandler() {
		subscriptions = new UpdatingEventRegistrar<>();

		listener = new WindowOrientationListener(ThisApplication.context(), null) {
			@Override
			public void onProposedRotationChanged(int rotation) {
				listener.setCurrentRotation(rotation); //Might help reduce incorrect device orientation predictions

				if (lastRotation == null || lastRotation != rotation) {
					subscriptions.report(new AndroidSurfaceRotation(rotation).orientation());
					lastRotation = rotation;
				}
			}
		};

		if (!listener.canDetectOrientation())
			//A user of an HTC One X reported that he/she got this error. Apparently system auto rotation also wasn't working, so it was probably a problem with the phone.
			EnvironmentError.report(R.string.missing_accelerometer_message);
	}

	@Override
	protected void start() {
		lastRotation = null;
		listener.setCurrentRotation(-1);
		listener.enable();
	}

	@Override
	protected void stop() {
		listener.disable();
	}

	public EventRegistrar<Orientation> onRotationChanged() {
		return subscriptions;
	}

}
