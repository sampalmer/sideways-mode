package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import android.content.Context;
import android.os.Vibrator;


public class OrientationCalculator extends ExternalResource {
	
	private static final Orientation DEFAULT = Orientation.NATURAL; 
	
	private Orientation device = DEFAULT;
	private Orientation user = DEFAULT;
	
	private Context context;

	private final UpdatingEventRegistrar<Void> listener;

	public OrientationCalculator(final Context context) {
		listener = new UpdatingEventRegistrar<Void>(new UserErrorHandler(context));
		this.context = context;
	}
	
	@Override
	protected void start() throws EnvironmentException {
	}
	
	@Override
	protected void stop() throws EnvironmentException {
		device = DEFAULT;
		user = DEFAULT;
		reportUpdate();
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
		reportUpdate();
		
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
