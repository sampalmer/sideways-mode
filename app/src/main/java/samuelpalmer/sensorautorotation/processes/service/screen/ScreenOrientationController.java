package samuelpalmer.sensorautorotation.processes.service.screen;

import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.OrientationCalculator;
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

	private static <T> void set(Setting<T> setting, T value) {
		try {
			setting.set(value);
		} catch (SettingWriteFailedException e) {
			//Because the developer can't do anything about this; it seems to be a problem on the device.
			EnvironmentError.report(R.string.setting_write_failure_summary, R.string.system_setting_write_failure_message, e.value, e.key);
		}
	}

	private final EventHandler systemAutoRotationChanged = new EventHandler() {
		@Override
		public void update() {
			checkIfSystemAutoRotationWasEnabled();
		}
	};

	private void checkIfSystemAutoRotationWasEnabled() {
		boolean accelerometerRotation = settings.accelerometerRotation.get();
		if (accelerometerRotation) {
			//So when the service is stopped, the new auto rotation setting will be reapplied to the system. (Otherwise the user's change to system auto rotation could be reverted.)
			calculator.setAutoRotate(true);
			EnvironmentError.report(R.string.system_rotation_enabled_summary, R.string.system_rotation_enabled_message);
		}
	}

	private final EventHandler userRotationChanged = new EventHandler() {
		@Override
		public void update() {
			EnvironmentError.report(R.string.user_rotation_changed_summary, R.string.user_rotation_changed_message);
		}
	};


}
