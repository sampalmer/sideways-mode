package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.screen.Screen;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.ScreenOnBehaviour;


public class OrientationCalculator {

	private static final Orientation DEFAULT = Orientation.NATURAL;
	private final ApplicationSettings settings;
	private final Screen screen;
	private final ApplicationState state;

	private Orientation device = DEFAULT;
	private boolean deviceSpecified;

	private final UpdatingEventRegistrar<Void> listener;

	public OrientationCalculator(ApplicationSettings settings, Screen screen, ApplicationState state) {
		this.settings = settings;
		this.screen = screen;
		this.state = state;
		listener = new UpdatingEventRegistrar<>();
	}

	public void reset() {
		device = DEFAULT;
		Orientation newUser = DEFAULT;

		ScreenOnBehaviour screenOnBehaviour = settings.screenOnBehaviour().get();
		if (screenOnBehaviour == ScreenOnBehaviour.LOCK_SCREEN_ROTATION)
			state.set(false, newUser);
		else
			state.user(newUser);

		reportUpdate();
	}

	public boolean autoRotate() {
		return state.autoRotate();
	}

	public void setAutoRotate(boolean newValue) {
		/* Since the current app/window might be overriding the current proposed orientation.
		 Matching the actual screen orientation seems to provide a more consistent and intuitive user experience.
		 Locking the screen orientation locks the actual orientation rather than the proposed one.
		 Unlocking the screen orientation (AKA locking the user orientation) locks the user orientation derived from the screen. */
		inferUserFromScreen();
		state.autoRotate(newValue);
		reportUpdate();
	}

	private void inferUserFromScreen() {
		state.user(inferredUser());
	}

	private Orientation inferredUser() {
		return Orientation.NATURAL.plus(device.minus(screen.realOrientation()));
	}

	public void setDevice(Orientation newDevice) {

		if (!autoRotate()) {
			Rotation deviceChange = newDevice.minus(device);
			state.user(state.user().plus(deviceChange));
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
		return Orientation.NATURAL.minus(state.user());
	}

}
