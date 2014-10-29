package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemConfig;
import samuelpalmer.sensorautorotation.systemconfiguration.TypeConverters;
import android.provider.Settings;
import android.view.Surface;

public class Screen {

	private final Integer originalUserRotation;
	private final Boolean originalAccelerometerRotation;
	private SystemConfig config;
	
	public Screen(SystemConfig config) throws EnvironmentException {
		this.config = config;
		
		originalAccelerometerRotation = config.getOrNull(Settings.System.ACCELEROMETER_ROTATION, TypeConverters.bool);
		try {
			config.set(Settings.System.ACCELEROMETER_ROTATION, false, TypeConverters.bool);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
		
		originalUserRotation = config.getOrNull(Settings.System.USER_ROTATION, TypeConverters.integer);
	}
	
	public void set(Orientation value) throws EnvironmentException {
		int surfaceRotation = value.androidSurfaceRotation();
		
		try {
			config.set(Settings.System.USER_ROTATION, surfaceRotation, TypeConverters.integer);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}

	public void close() throws EnvironmentException {
		try {
			config.set(Settings.System.USER_ROTATION, originalUserRotation, TypeConverters.integer);
			config.set(Settings.System.ACCELEROMETER_ROTATION, originalAccelerometerRotation, TypeConverters.bool);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}

	public Orientation originalUserOrientation() {
		return Orientation.fromAndroidSurfaceRotation(originalUserRotation == null ? Surface.ROTATION_0 : originalUserRotation);
	}
	
	public boolean originalAutoRotation() {
		return originalAccelerometerRotation == null ? true : originalAccelerometerRotation;
	}
}
