package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import android.content.Context;
import android.os.Vibrator;


public class OrientationCalculator extends ExternalResource {
	
	private static final Orientation DEFAULT = Orientation.NATURAL; 
	
	private Orientation device = DEFAULT;
	private Orientation user = DEFAULT;
	
	private ScreenOrientationController screenOrientationController;
	private boolean covered;
	private Context context;

	private final UpdatingEventRegistrar<Void> listener;

	private ProximitySensorHandler proximitySensor;
	
	public OrientationCalculator(final Context context, ProximitySensorHandler proximitySensor, ScreenOrientationController screenOrientationController) {
		listener = new UpdatingEventRegistrar<Void>(new UserErrorHandler(context));
		this.context = context;
		this.proximitySensor = proximitySensor;
		this.screenOrientationController = screenOrientationController;
	}
	
	@Override
	protected void start() throws EnvironmentException {
		proximitySensor.onChange().subscribe(subscriber);
	}
	
	@Override
	protected void stop() throws EnvironmentException {
		proximitySensor.onChange().unsubscribe(subscriber);
		
		device = DEFAULT;
		user = DEFAULT;
		reportUpdate();
	}
	
	private final EventHandler<ProximitySensorCoveredState> subscriber = new EventHandler<ProximitySensorCoveredState>() {
		
		@Override
		public void update(ProximitySensorCoveredState event) throws EnvironmentException {
			if (event.isCovered() && !covered)
				vibrate(40);
			
			covered = event.isCovered();
		}
	};
	
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
		
		listener.report(null);
	}
	
	public EventRegistrar<Void> onChange() {
		return listener;
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
	
}
