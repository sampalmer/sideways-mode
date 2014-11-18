package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.os.Vibrator;


public class OrientationCalculator implements ProximitySensorHandler.Listener {
	
	private Orientation device;
	private Orientation user = Orientation.NATURAL;
	
	private Screen screen;
	private boolean covered;
	private Context context;
	
	public OrientationCalculator(final Context context, Screen screen) {
		this.context = context;
		this.screen = screen;
	}
	
	public void setDevice(Orientation newDevice) throws EnvironmentException {
		
		if (covered) {
			if (device != null) {
				Rotation deviceChange = newDevice.minus(device);
				if (deviceChange.clockwiseCount() != 0) {
					Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
				    vibrator.vibrate(75);
				    user = user.plus(deviceChange);
				}
			}
		}
		else {
			Orientation proposedScreenOrientation = newDevice.plus(Orientation.NATURAL.minus(user));
			screen.propose(proposedScreenOrientation);
		}
			
		device = newDevice;
	}

	@Override
	public void stateChanged(boolean proximitySensorCovered) {
		covered = proximitySensorCovered;
	}
	
}
