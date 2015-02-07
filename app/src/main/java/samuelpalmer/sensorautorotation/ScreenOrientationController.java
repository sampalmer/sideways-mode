package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.systemconfiguration.Setting;
import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;

public class ScreenOrientationController extends State {

	private SystemSettings settings;
	private Boolean originalAccelerometerRotation;
	private Context context;

	private final BroadcastReceiver configChangedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			systemConfigChanged();
		}

	};

	public ScreenOrientationController(Context context, SystemSettings settings) {
		this.context = context;
		this.settings = settings;
		onOrientationChanged = new UpdatingEventRegistrar<>();
	}

	@Override
	protected void start() {
		Orientation realOrientation = realOrientation();
		propose(realOrientation);

		originalAccelerometerRotation = settings.accelerometerRotation.getOrNull();
		set(settings.accelerometerRotation, false);

		propose(realOrientation); //Just in case disabling system rotation reset this.

		context.registerReceiver(configChangedReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
	}

	@Override
	protected void stop() {
		context.unregisterReceiver(configChangedReceiver);

		set(settings.userRotation, Orientation.NATURAL);
		set(settings.accelerometerRotation, originalAccelerometerRotation);
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

	public Orientation realOrientation() {
		WindowManager windows = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		AndroidSurfaceRotation rotation = new AndroidSurfaceRotation(windows.getDefaultDisplay().getRotation());
		return rotation.orientation();
	}

	private Orientation proposedOrientation() {
		Orientation result = settings.userRotation.getOrNull();
		if (result == null)
			result = Orientation.NATURAL;

		return result;
	}

	private void systemConfigChanged() {
		checkIfSystemAutoRotationWasEnabled();
		onOrientationChanged.report(null);
	}

	private void checkIfSystemAutoRotationWasEnabled() {
		Boolean accelerometerRotation = settings.accelerometerRotation.getOrNull();
		if (accelerometerRotation != null && accelerometerRotation) {
			originalAccelerometerRotation = true;
			new EnvironmentErrorNotifier(context).handle("System screen auto-rotation was enabled.");
		}
	}

	public EventRegistrar<Void> onOrientationChanged() {
		return onOrientationChanged;
	}

	private final UpdatingEventRegistrar<Void> onOrientationChanged;

	@Override
	public String toString() {
		return proposedOrientation().toString();
	}

}
