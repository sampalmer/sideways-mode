package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.android.WindowOrientationListener;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;


public class OrientationSensorHandler extends State {

	private WindowOrientationListener listener;
	private final UpdatingEventRegistrar<Orientation> subscriptions;
	private Integer lastRotation;

	public OrientationSensorHandler(final Context context) throws EnvironmentException {
		subscriptions = new UpdatingEventRegistrar<>(new UserErrorHandler(context));

		listener = new WindowOrientationListener(context, null) {
			@Override
			public void onProposedRotationChanged(int rotation) {
				if (lastRotation == null || lastRotation != rotation) {
					subscriptions.report(new AndroidSurfaceRotation(rotation).orientation());
					lastRotation = rotation;
				}
			}
		};

		if (!listener.canDetectOrientation())
			throw new EnvironmentException("Couldn't find an accelerometer");
	}

	@Override
	protected void start() {
		lastRotation = null;
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
