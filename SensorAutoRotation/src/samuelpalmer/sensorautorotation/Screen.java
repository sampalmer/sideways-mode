package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.systemconfiguration.AndroidSystemSettingSource;
import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;
import android.content.Context;
import android.view.WindowManager;

public class Screen {
	
	private SystemSettings settings;
	private final Boolean originalAccelerometerRotation;
	private Context context;
	
	public Screen(Context context) throws EnvironmentException {
		this.context = context;
		settings = new SystemSettings(new AndroidSystemSettingSource(context.getContentResolver()));
		
		originalAccelerometerRotation = settings.accelerometerRotation.getOrNull();
		
		try {
			settings.accelerometerRotation.set(false);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}
	
	public void set(Orientation value) throws EnvironmentException {
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
			settings.userRotation.set(Orientation.NATURAL);
			settings.accelerometerRotation.set(originalAccelerometerRotation);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}

	public Orientation get() {
		WindowManager x = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = x.getDefaultDisplay().getRotation();
		return Orientation.fromAndroidSurfaceRotation(rotation);
	}
	
	@Override
	public String toString() {
		return get().toString();
	}
}
