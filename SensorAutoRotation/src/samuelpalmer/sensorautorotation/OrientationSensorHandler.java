package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.android.WindowOrientationListener;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import android.content.Context;


public class OrientationSensorHandler extends ExternalResource {

	private WindowOrientationListener listener;
	private final UpdatingEventRegistrar<AndroidSurfaceRotation> subscriptions;
	private Integer lastRotation;
	
	public OrientationSensorHandler(final Context context) throws EnvironmentException {
		subscriptions = new UpdatingEventRegistrar<AndroidSurfaceRotation>(new UserErrorHandler(context));
		
		listener = new WindowOrientationListener(context, null) {
			@Override
			public void onProposedRotationChanged(int rotation) {
				if (lastRotation == null || lastRotation != rotation) {
					subscriptions.report(new AndroidSurfaceRotation(rotation));
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
	
	public EventRegistrar<AndroidSurfaceRotation> onRotationChanged() {
		return subscriptions;
	}
	
}
