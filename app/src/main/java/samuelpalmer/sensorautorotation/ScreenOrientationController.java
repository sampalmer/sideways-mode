package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.settings.system.Setting;
import samuelpalmer.sensorautorotation.settings.system.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ScreenOrientationController extends State {

	private final SystemSettings settings;
	private final Screen screen;
	private final OrientationCalculator calculator;
	private final Context context;

	private final EventHandler<Void> configChangedReceiver = new EventHandler<Void>() {
		@Override
		public void update(Void event) {
			systemConfigChanged();
		}
	};

	public ScreenOrientationController(Context context, SystemSettings settings, Screen screen, OrientationCalculator calculator) {
		this.context = context;
		this.settings = settings;
		this.screen = screen;
		this.calculator = calculator;
	}

	@Override
	protected void start() {
		settings.accelerometerRotation.onExternalChange.subscribe(configChangedReceiver);

		Orientation realOrientation = screen.realOrientation();
		propose(realOrientation);

		set(settings.accelerometerRotation, false);
		propose(realOrientation); //Just in case disabling system rotation reset this.
	}

	@Override
	protected void stop() {
		set(settings.accelerometerRotation, calculator.autoRotate());
		set(settings.userRotation, Orientation.NATURAL);

		settings.accelerometerRotation.onExternalChange.unsubscribe(configChangedReceiver);
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
			new EnvironmentErrorNotifier(context).handle(e.getMessage());
		}
	}

	private void systemConfigChanged() {
		checkIfSystemAutoRotationWasEnabled();
	}

	private void checkIfSystemAutoRotationWasEnabled() {
		boolean accelerometerRotation = settings.accelerometerRotation.get();
		if (accelerometerRotation) {
			//So when the service is stopped, the new auto rotation setting will be reapplied to the system. (Otherwise the user's change to system auto rotation could be reverted.)
			calculator.setAutoRotate(true);
			new EnvironmentErrorNotifier(context).handle("System screen auto-rotation was enabled.");
		}
	}

}
