package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.os.Vibrator;


public class OrientationCalculator
	extends ExternalResource
	implements ProximitySensorHandler.Listener {
	
	private static final Orientation DEFAULT = Orientation.NATURAL; 
	
	private Orientation device = DEFAULT;
	private Orientation user = DEFAULT;
	
	private ScreenOrientationController screenOrientationController;
	private boolean covered;
	private Context context;

	private Listener listener;
	
	public OrientationCalculator(final Context context, ScreenOrientationController screenOrientationController) {
		this.context = context;
		this.screenOrientationController = screenOrientationController;
	}
	
	@Override
	protected void start() throws EnvironmentException {
	}
	
	@Override
	protected void stop() throws EnvironmentException {
		device = DEFAULT;
		user = DEFAULT;
		screenOrientationController.propose(proposedScreenOrientation());
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
		
		reportUpdate();
	}

	private void vibrate(int durationMilliseconds) {
		Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		vibrator.vibrate(durationMilliseconds);
	}
	
	public Orientation getDevice() {
		return device;
	}
	
	private void reportUpdate() {
		if (!proposedScreenOrientation().equals(Orientation.UPSIDE_DOWN))
			try {
				screenOrientationController.propose(proposedScreenOrientation());
			} catch (EnvironmentException e) {
				new UserErrorHandler(context).handle(e);
			}
		
		if (listener != null)
			listener.update();
	}

	private Orientation proposedScreenOrientation() {
		return device.plus(offset());
	}

	private Rotation offset() {
		return Orientation.NATURAL.minus(user);
	}
	
	public void resetUser() {
		user = DEFAULT;
		reportUpdate();
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
