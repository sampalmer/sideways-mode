package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import android.content.Context;


public class OrientationCalculator extends ExternalResource {
	
	private static final Orientation DEFAULT = Orientation.NATURAL; 
	
	private Orientation device;
	private Orientation user;
	
	private final UpdatingEventRegistrar<Void> listener;

	public OrientationCalculator(final Context context) {
		listener = new UpdatingEventRegistrar<Void>(new UserErrorHandler(context));
	}
	
	@Override
	protected void start() throws EnvironmentException {
		device = DEFAULT;
		user = DEFAULT;
	}
	
	@Override
	protected void stop() throws EnvironmentException {
		device = DEFAULT;
		user = DEFAULT;
		reportUpdate();
	}
	
	public void setDevice(Orientation newDevice) {
		device = newDevice;
		reportUpdate();
	}
	
	private void reportUpdate() {
		listener.report(null);
	}
	
	public EventRegistrar<Void> onChange() {
		return listener;
	}
	
	public Orientation proposedScreenOrientation() {
		return device.plus(userOffset());
	}

	private Rotation userOffset() {
		return Orientation.NATURAL.minus(user);
	}
	
	public void resetUser() {
		setUser(DEFAULT);
	}

	private void setUser(Orientation newUser) {
		user = newUser;
		reportUpdate();
	}
	
	public void offsetUser(Rotation offset) {
		setUser(user.plus(offset));
	}
}
