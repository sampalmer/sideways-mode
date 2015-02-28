package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import samuelpalmer.sensorautorotation.systemconfiguration.Setting;
import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;

public class ScreenOrientationController extends State {

	private SystemSettings settings;
	private final Screen screen;
	private final OrientationCalculator calculator;
	private Context context;

	private final BroadcastReceiver configChangedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
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
		Orientation realOrientation = screen.realOrientation();
		propose(realOrientation);

		set(settings.accelerometerRotation, false);

		propose(realOrientation); //Just in case disabling system rotation reset this.

		context.registerReceiver(configChangedReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
	}

	@Override
	protected void stop() {
		context.unregisterReceiver(configChangedReceiver);

		set(settings.userRotation, Orientation.NATURAL);
		set(settings.accelerometerRotation, !calculator.isRecalibrating());
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
			new EnvironmentErrorNotifier(context).handle(e.getMessage());
		}
	}

	private Orientation proposedOrientation() {
		return settings.userRotation.get();
	}

	private void systemConfigChanged() {
		checkIfSystemAutoRotationWasEnabled();
	}

	private void checkIfSystemAutoRotationWasEnabled() {
		boolean accelerometerRotation = settings.accelerometerRotation.get();
		if (accelerometerRotation) {
			calculator.setRecalibrating(false);
			new EnvironmentErrorNotifier(context).handle("System screen auto-rotation was enabled.");
		}
	}

	@Override
	public String toString() {
		return proposedOrientation().toString();
	}

}
