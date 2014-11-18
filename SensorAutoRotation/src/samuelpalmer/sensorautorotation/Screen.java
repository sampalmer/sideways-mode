package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.systemconfiguration.AndroidSystemSettingSource;
import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.WindowManager;

public class Screen {
	
	private SystemSettings settings;
	private Boolean originalAccelerometerRotation;
	private Context context;
	
	private final BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			systemConfigChanged();
		}
		
	};
	
	public Screen(Context context) throws EnvironmentException {
		this.context = context;
		settings = new SystemSettings(new AndroidSystemSettingSource(context.getContentResolver()));
		
		Orientation realOrientation = realOrientation();
		
		originalAccelerometerRotation = settings.accelerometerRotation.getOrNull();
		
		try {
			settings.accelerometerRotation.set(false);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
		
		propose(realOrientation);
		
		context.registerReceiver(receiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
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

	public void close() throws EnvironmentException {
		try {
			context.unregisterReceiver(receiver);
			settings.accelerometerRotation.set(originalAccelerometerRotation);
			settings.userRotation.set(Orientation.NATURAL);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}

	private Orientation realOrientation() {
		WindowManager windows = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = windows.getDefaultDisplay().getRotation();
		return Orientation.fromAndroidSurfaceRotation(rotation);
	}
	
	private Orientation proposedOrientation() {
		Orientation result = settings.userRotation.getOrNull();
		if (result == null)
			result = Orientation.NATURAL;
		
		return result;
	}
	
	private void systemConfigChanged() {
		try {
			Boolean originalAccelerometerRotation = settings.accelerometerRotation.getOrNull();
			if (originalAccelerometerRotation == true)
				throw new EnvironmentException("System screen auto-rotation was enabled.");
		} catch (EnvironmentException e) {
			new UserErrorHandler(context).handle(e);
		}
	}
	
	@Override
	public String toString() {
		return proposedOrientation().toString();
	}
}
