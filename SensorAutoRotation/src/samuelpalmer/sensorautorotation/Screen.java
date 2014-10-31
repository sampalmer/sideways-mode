package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;
import android.view.Surface;

public class Screen {
	
	private SystemSettings settings;
	private final Integer originalUserRotation;
	private final Boolean originalAccelerometerRotation;
	
	public Screen(SystemSettings systemSettings) {
		settings = systemSettings;
		
		originalUserRotation = settings.userRotation.getOrNull();
		originalAccelerometerRotation = settings.accelerometerRotation.getOrNull();
		
		try {
			settings.accelerometerRotation.set(false);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}
	
	public void set(Orientation value) {
		int surfaceRotation = value.androidSurfaceRotation();
		
		try {
			settings.userRotation.set(surfaceRotation);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}

	public void close() {
		try {
			settings.userRotation.set(originalUserRotation);
			settings.accelerometerRotation.set(originalAccelerometerRotation);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}

	public Orientation get() {
		Integer androidSurfaceRotation = settings.userRotation.getOrNull();
		return Orientation.fromAndroidSurfaceRotation(androidSurfaceRotation == null ? Surface.ROTATION_0 : androidSurfaceRotation);
	}
	
	@Override
	public String toString() {
		return get().toString();
	}
}
