package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.android.AndroidSystemSettingSource;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.WindowManager;

public class ScreenOrientationController extends ExternalResource {
	
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
		onOrientationChanged = new UpdatingEventRegistrar<Void>(new UserErrorHandler(context));
	}
	
	@Override
	protected void start() throws EnvironmentException {
		Orientation realOrientation = realOrientation();
		propose(realOrientation);
		
		originalAccelerometerRotation = settings.accelerometerRotation.getOrNull();
		try {
			settings.accelerometerRotation.set(false);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
		
		propose(realOrientation); //Just in case disabling system rotation reset this.
		
		context.registerReceiver(configChangedReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
	}

	@Override
	protected void stop() throws EnvironmentException {
		context.unregisterReceiver(configChangedReceiver);
		
		try {
			settings.userRotation.set(Orientation.NATURAL);
			settings.accelerometerRotation.set(originalAccelerometerRotation);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}
	
	public void propose(Orientation value) throws EnvironmentException {
		if (value == null)
			throw new IllegalArgumentException();
		
		try {
			settings.userRotation.set(value);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}
	
	public Orientation realOrientation() {
		WindowManager windows = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
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
		try {
			Boolean accelerometerRotation = settings.accelerometerRotation.getOrNull();
			if (accelerometerRotation != null && accelerometerRotation == true) {
				originalAccelerometerRotation = accelerometerRotation;
				throw new EnvironmentException("System screen auto-rotation was enabled.");
			}
		} catch (EnvironmentException e) {
			new UserErrorHandler(context).handle(e);
		}
		
		onOrientationChanged.report(null);
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
