package samuelpalmer.sensorautorotation;

import android.content.ContentResolver;
import android.provider.Settings;
import android.view.Surface;

public class PassiveOrientationChanger {

	private final ContentResolver contentResolver;
	private final int originalUserRotation;
	private final int originalAccelerometerRotation;
	
	public PassiveOrientationChanger(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
		
		originalAccelerometerRotation = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 0);
		set(Settings.System.ACCELEROMETER_ROTATION, 0, "Failed to disable system auto-rotation"); //TODO: Make this a warning for the user
		
		originalUserRotation = Settings.System.getInt(contentResolver, Settings.System.USER_ROTATION, Surface.ROTATION_0);
	}
	
	public void set(Orientation value) {
		int surfaceRotation = value.androidSurfaceRotation();
		set(Settings.System.USER_ROTATION, surfaceRotation, "Couldn't change orientation");
	}

	public void close() {
		set(Settings.System.USER_ROTATION, originalUserRotation, "Couldn't restore original orientation");
		set(Settings.System.ACCELEROMETER_ROTATION, originalAccelerometerRotation, "Couldn't restore original auto-rotation value");
	}

	private void set(String setting, int value, String failureMessage) {
		boolean successful = Settings.System.putInt(contentResolver, setting, value);
		if (!successful)
			throw new RuntimeException(failureMessage);
	}

	public Orientation getOriginal() {
		return Orientation.fromAndroidSurfaceRotation(originalUserRotation);
	}
}
