package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;

public class Screen {
	
	private SystemSettings settings;
	private final Orientation originalUserRotation;
	private final Boolean originalAccelerometerRotation;
	
	public Screen(SystemSettings systemSettings) throws EnvironmentException {
		settings = systemSettings;
		
		originalUserRotation = settings.userRotation.getOrNull();
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
			settings.userRotation.set(originalUserRotation);
			settings.accelerometerRotation.set(originalAccelerometerRotation);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}

	public Orientation get() {
		Orientation result = settings.userRotation.getOrNull();
		return result == null ? Orientation.NATURAL : result;
	}
	
	@Override
	public String toString() {
		return get().toString();
	}
}
