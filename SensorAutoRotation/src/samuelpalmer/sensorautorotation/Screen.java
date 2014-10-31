package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import android.view.Surface;

public class Screen {
	
	private SystemSettings settings;
	private final Integer originalUserRotation;
	private final Boolean originalAccelerometerRotation;
	
	public Screen(SystemSettings systemSettings) {
		settings = systemSettings;
		
		originalAccelerometerRotation = settings.accelerometerRotation.getOrNull();
		try {
			settings.accelerometerRotation.set(false);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
		
		originalUserRotation = settings.userRotation.getOrNull();
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

	public Orientation originalUserOrientation() {
		return Orientation.fromAndroidSurfaceRotation(originalUserRotation == null ? Surface.ROTATION_0 : originalUserRotation);
	}
}
