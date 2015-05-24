package samuelpalmer.sensorautorotation.screen;

import samuelpalmer.sensorautorotation.EnvironmentErrorNotifier;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.OrientationCalculator;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.system.Setting;
import samuelpalmer.sensorautorotation.settings.system.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ScreenOrientationController extends State {

	private final SystemSettings settings;
	private final Screen screen;
	private final OrientationCalculator calculator;

	public ScreenOrientationController(SystemSettings settings, Screen screen, OrientationCalculator calculator) {
		this.settings = settings;
		this.screen = screen;
		this.calculator = calculator;
	}

	@Override
	protected void start() {
		settings.accelerometerRotation.onChange().subscribe(systemAutoRotationChanged);

		Orientation realOrientation = screen.realOrientation();
		propose(realOrientation);

		set(settings.accelerometerRotation, false);
		propose(realOrientation); //Just in case disabling system rotation reset this.

		settings.userRotation.onExternalChange().subscribe(userRotationChanged);
	}

	@Override
	protected void stop() {
		settings.userRotation.onExternalChange().unsubscribe(userRotationChanged);
		settings.accelerometerRotation.onChange().unsubscribe(systemAutoRotationChanged);

		set(settings.accelerometerRotation, calculator.autoRotate());
		propose(Orientation.NATURAL);
	}

	public void propose(Orientation value) {
		if (value == null)
			throw new IllegalArgumentException();

		set(settings.userRotation, value);
	}

	private <T> void set(Setting<T> setting, T value) {
		try {
			setting.set(value);
		} catch (SettingWriteFailedException e) {
			//Because the developer can't do anything about this; it seems to be a problem on the device.
			new EnvironmentErrorNotifier().handle(R.string.system_setting_write_failure_message, e.value, e.key);
		}
	}

	private final EventHandler<Void> systemAutoRotationChanged = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			checkIfSystemAutoRotationWasEnabled();
		}
	};

	private void checkIfSystemAutoRotationWasEnabled() {
		boolean accelerometerRotation = settings.accelerometerRotation.get();
		if (accelerometerRotation) {
			//So when the service is stopped, the new auto rotation setting will be reapplied to the system. (Otherwise the user's change to system auto rotation could be reverted.)
			calculator.setAutoRotate(true);
			new EnvironmentErrorNotifier().handle(R.string.system_rotation_enabled_message);
		}
	}

	private final EventHandler<Void> userRotationChanged = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			new EnvironmentErrorNotifier().handle(R.string.user_rotation_changed_message);
		}
	};


}
