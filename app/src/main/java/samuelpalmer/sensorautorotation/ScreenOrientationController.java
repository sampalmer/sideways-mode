package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.WindowManager;

import samuelpalmer.sensorautorotation.android.AndroidSystemSettingSource;
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

	public ScreenOrientationController(Context context) {
		this.context = context;
		settings = new SystemSettings(new AndroidSystemSettingSource(context.getContentResolver()));
		onOrientationChanged = new UpdatingEventRegistrar<>(new EnvironmentErrorNotifier(context));
	}

	@Override
	protected void start() throws EnvironmentException {
		Orientation realOrientation = realOrientation();
		propose(realOrientation);

		originalAccelerometerRotation = settings.accelerometerRotation.getOrNull();
		set(settings.accelerometerRotation, false);

		propose(realOrientation); //Just in case disabling system rotation reset this.

		context.registerReceiver(configChangedReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
	}

	@Override
	protected void stop() throws EnvironmentException {
		context.unregisterReceiver(configChangedReceiver);

		set(settings.userRotation, Orientation.NATURAL);
		set(settings.accelerometerRotation, originalAccelerometerRotation);
	}

	public void propose(Orientation value) throws EnvironmentException {
		if (value == null)
			throw new IllegalArgumentException();

		set(settings.userRotation, value);
	}

	private <T> void set(Setting<T> setting, T value) throws EnvironmentException {
		try {
			setting.set(value);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
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
		try {
			Boolean accelerometerRotation = settings.accelerometerRotation.getOrNull();
			if (accelerometerRotation != null && accelerometerRotation) {
				originalAccelerometerRotation = accelerometerRotation;
				throw new EnvironmentException("System screen auto-rotation was enabled. Screen orientation probably won't work correctly.");
			}
		} catch (EnvironmentException e) {
			new EnvironmentErrorNotifier(context).handle(e);
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
