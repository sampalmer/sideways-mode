package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;


public class OrientationCalculator {

	private static final Orientation DEFAULT = Orientation.NATURAL;
	private final Context context;
	private final Screen screen;

	private Orientation device = DEFAULT;
	private Orientation user = DEFAULT;
	private boolean deviceSpecified;
	private boolean recalibrating;

	private final UpdatingEventRegistrar<Void> listener;

	public OrientationCalculator(Context context, Screen screen, SystemSettings settings) {
		this.context = context;
		this.screen = screen;
		listener = new UpdatingEventRegistrar<>();

		Boolean accelerometerRotation = settings.accelerometerRotation.getOrNull();
		recalibrating = accelerometerRotation == null || !accelerometerRotation;
	}

	public void reset() {
		device = DEFAULT;
		user = DEFAULT;

		Settings.ScreenOnBehaviour screenOnBehaviour = new Settings(context).screenOnBehaviour().get();
		if (screenOnBehaviour == Settings.ScreenOnBehaviour.LOCK_SCREEN_ROTATION)
			recalibrating = true;

		reportUpdate();
	}

	public boolean isRecalibrating() {
		return recalibrating;
	}

	public void setRecalibrating(boolean newValue) {
		if (newValue) {
			inferUserFromScreen();
			reportUpdate();
		}

		recalibrating = newValue;
	}

	private void inferUserFromScreen() {
		user = Orientation.NATURAL.plus(device.minus(screen.realOrientation()));
	}

	public void setDevice(Orientation newDevice) {

		if (recalibrating) {
			Rotation deviceChange = newDevice.minus(device);
			user = user.plus(deviceChange);
		}

		device = newDevice;

		if (!deviceSpecified) {
			inferUserFromScreen();
			deviceSpecified = true;
		}

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

}
