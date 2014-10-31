package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.systemconfiguration.Setting;
import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemConfig;
import samuelpalmer.sensorautorotation.systemconfiguration.types.TypeConverters;
import android.provider.Settings;
import android.view.Surface;

public class Screen {
	
	private final Setting<Boolean> accelerometerRotation;
	private final Setting<Integer> userRotation;
	
	private final Integer originalUserRotation;
	private final Boolean originalAccelerometerRotation;
	
	public Screen(SystemConfig config) {
		accelerometerRotation = new Setting<Boolean>(config, Settings.System.ACCELEROMETER_ROTATION, TypeConverters.bool);
		userRotation = new Setting<Integer>(config, Settings.System.USER_ROTATION, TypeConverters.integer);
		
		originalAccelerometerRotation = accelerometerRotation.getOrNull();
		try {
			accelerometerRotation.set(false);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
		
		originalUserRotation = userRotation.getOrNull();
	}
	
	public void set(Orientation value) {
		int surfaceRotation = value.androidSurfaceRotation();
		
		try {
			userRotation.set(surfaceRotation);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}

	public void close() {
		try {
			userRotation.set(originalUserRotation);
			accelerometerRotation.set(originalAccelerometerRotation);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}

	public Orientation originalUserOrientation() {
		return Orientation.fromAndroidSurfaceRotation(originalUserRotation == null ? Surface.ROTATION_0 : originalUserRotation);
	}
}
