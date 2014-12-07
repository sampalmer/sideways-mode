package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.facedetection.CameraOrientationCalculator;
import android.content.Context;
import android.os.Vibrator;


public class OrientationCalculator extends ExternalResource {
	
	private static final Orientation DEFAULT = Orientation.NATURAL; 
	
	private Orientation device = DEFAULT;
	private Orientation user = DEFAULT;
	
	private Context context;

	private final UpdatingEventRegistrar<Void> listener;

	private CameraOrientationCalculator camera;

	public OrientationCalculator(final Context context, CameraOrientationCalculator camera) {
		this.camera = camera;
		listener = new UpdatingEventRegistrar<Void>(new UserErrorHandler(context));
		this.context = context;
	}
	
	private final EventHandler<Orientation> cameraSubscriber = new EventHandler<Orientation>() {
		
		@Override
		public void update(Orientation cameraUser) throws EnvironmentException {
			if (cameraUser != null)
				user = Orientation.NATURAL.plus(device.minus(cameraUser));
			
			reportUpdate();
		}
	};
	
	@Override
	protected void start() throws EnvironmentException {
		camera.onDetected().subscribe(cameraSubscriber);
	}
	
	@Override
	protected void stop() throws EnvironmentException {
		device = DEFAULT;
		user = DEFAULT;
		reportUpdate();
		camera.onDetected().unsubscribe(cameraSubscriber);
	}
	
	public void setRecalibrating(boolean recalibrating) {
		if (recalibrating && !this.recalibrating)
			vibrate(40);
		
		this.recalibrating = recalibrating;		
	}
	
	private boolean recalibrating;
	
	public void setDevice(Orientation newDevice) {
		
		if (recalibrating) {
			Rotation deviceChange = newDevice.minus(device);
			if (deviceChange.clockwiseCount() != 0) {
				vibrate(85);
			    user = user.plus(deviceChange);
			}
		}
			
		device = newDevice;
		
		camera.detect();
	}
	
	private void vibrate(int durationMilliseconds) {
		Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(durationMilliseconds);
	}
	
	private void reportUpdate() {
		listener.report(null);
	}
	
	public EventRegistrar<Void> onChange() {
		return listener;
	}
	
	public Orientation proposedScreenOrientation() {
		return device.plus(offset());
	}

	private Rotation offset() {
		return Orientation.NATURAL.minus(user);
	}
	
	public void resetUser() {
		user = DEFAULT;
		reportUpdate();
	}
	
}
