package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.os.Vibrator;


public class OrientationCalculator
	extends ExternalResource
	implements ProximitySensorHandler.Listener {
	
	private static final Orientation DEFAULT_USER = Orientation.NATURAL; 
	
	private Orientation device = DEFAULT_USER;
	private Orientation user = DEFAULT_USER;
	
	private Screen screen;
	private boolean covered;
	private Context context;

	private Listener listener;
	
	public OrientationCalculator(final Context context, Screen screen) {
		this.context = context;
		this.screen = screen;
	}
	
	@Override
	protected void start() throws EnvironmentException {
		device = DEFAULT_USER;
		user = DEFAULT_USER;
	}
	
	@Override
	protected void stop() throws EnvironmentException {
		screen.propose(Orientation.NATURAL);
	}
	
	public void setDevice(Orientation newDevice) {
		
		if (covered) {
			Rotation deviceChange = newDevice.minus(device);
			if (deviceChange.clockwiseCount() != 0) {
				vibrate(85);
			    user = user.plus(deviceChange);
			}
		}
			
		device = newDevice;
		
		updateScreen();
	}

	private void vibrate(int durationMilliseconds) {
		Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(durationMilliseconds);
	}
	
	public Orientation getDevice() {
		return device;
	}
	
	private void updateScreen() {
		Orientation proposedScreenOrientation = device.plus(Orientation.NATURAL.minus(user));
		if (!proposedScreenOrientation.equals(Orientation.UPSIDE_DOWN))
			try {
				screen.propose(proposedScreenOrientation);
			} catch (EnvironmentException e) {
				new UserErrorHandler(context).handle(e);
			}
		
		updateListener();
	}
	
	public void resetUser() {
		user = DEFAULT_USER;
		updateScreen();
	}

	private void updateListener() {
		if (listener != null)
			listener.update();
	}
	
	public Orientation getUser() {
		return user;
	}
	
	public void listen(Listener listener) {
		if (this.listener != null)
			throw new RuntimeException("Already listening");
		
		this.listener = listener; 
	}
	
	public void stopListening() {
		if (listener == null)
			throw new RuntimeException("Not listening");
		
		listener = null;
	}
	
	public interface Listener {
		void update();
	}
	
	@Override
	public void update(boolean proximitySensorCovered) {
		if (proximitySensorCovered && !covered)
			vibrate(40);
		
		covered = proximitySensorCovered;
	}
	
}
