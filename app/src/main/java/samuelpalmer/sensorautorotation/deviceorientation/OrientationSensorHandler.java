package samuelpalmer.sensorautorotation.deviceorientation;

import android.content.Context;

import samuelpalmer.sensorautorotation.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.EnvironmentErrorNotifier;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;


public class OrientationSensorHandler extends State {

	private final WindowOrientationListener listener;
	private final UpdatingEventRegistrar<Orientation> subscriptions;
	private Integer lastRotation;

	public OrientationSensorHandler(final Context context) {
		subscriptions = new UpdatingEventRegistrar<>();

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
			//A user of an HTC One X reported that he/she got this error. Apparently system auto rotation also wasn't working, so it was probably a problem with the phone.
			new EnvironmentErrorNotifier(context).handle("This application needs an accelerometer to run, but it didn't find one in this device. If the device does have one, try using the device's \"Auto-rotate screen\" option. If that doesn't work, the accelerometer might be broken.");
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
