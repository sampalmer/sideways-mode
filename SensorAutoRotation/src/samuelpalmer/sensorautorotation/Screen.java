package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.systemconfiguration.AndroidSystemSettingSource;
import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;
import android.content.Context;
import android.view.WindowManager;

public class Screen {
	
	private SystemSettings settings;
	private Boolean originalAccelerometerRotation;
	private Context context;
	
	public Screen(Context context) throws EnvironmentException {
		this.context = context;
		settings = new SystemSettings(new AndroidSystemSettingSource(context.getContentResolver()));
		
		originalAccelerometerRotation = settings.accelerometerRotation.getOrNull();
		
		matchCurrentScreenRotation();
		
		try {
			settings.accelerometerRotation.set(false);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}

	public void matchCurrentScreenRotation() throws EnvironmentException {
		propose(orientation());
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
			settings.accelerometerRotation.set(originalAccelerometerRotation);
			settings.userRotation.set(Orientation.NATURAL);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}

	public Orientation orientation() {
		WindowManager windows = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = windows.getDefaultDisplay().getRotation();
		return Orientation.fromAndroidSurfaceRotation(rotation);
	}
	
	public void systemConfigChanged() throws EnvironmentException {
		matchCurrentScreenRotation();
		
		Boolean currentAccelerometerRotation = settings.accelerometerRotation.getOrNull();
		if (currentAccelerometerRotation == true) {
			originalAccelerometerRotation = currentAccelerometerRotation;
			throw new EnvironmentException("System screen auto-rotation was enabled.");
		}
	}
	
	@Override
	public String toString() {
		return orientation().toString();
	}
}
