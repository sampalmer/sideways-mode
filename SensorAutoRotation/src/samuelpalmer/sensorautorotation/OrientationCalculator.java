package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.os.Vibrator;


public class OrientationCalculator implements ProximitySensorHandler.Listener {
	
	private static final Orientation DEFAULT_USER = Orientation.NATURAL; 
	
	private Orientation device;
	private Orientation user = DEFAULT_USER;
	
	private Screen screen;
	private boolean covered;
	private Context context;
	
	public OrientationCalculator(final Context context, Screen screen) {
		this.context = context;
		this.screen = screen;
	}
	
	public void setDevice(Orientation newDevice) {
		
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
			
		device = newDevice;
		
		updateScreen();
	}

	private void updateScreen() {
		Orientation proposedScreenOrientation = device.plus(Orientation.NATURAL.minus(user));
		try {
			screen.propose(proposedScreenOrientation);
		} catch (EnvironmentException e) {
			new UserErrorHandler(context).handle(e);
		}
	}
	
	public void resetUser() {
		user = DEFAULT_USER;
		updateScreen();
	}
	
	@Override
	public void update(boolean proximitySensorCovered) {
		covered = proximitySensorCovered;
	}
	
}
