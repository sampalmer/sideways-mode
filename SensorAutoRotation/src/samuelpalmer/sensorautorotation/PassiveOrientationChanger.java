package samuelpalmer.sensorautorotation;

import android.content.ContentResolver;
import android.provider.Settings;
import android.view.Surface;

public class PassiveOrientationChanger implements OrientationChanger {

	private final ContentResolver contentResolver;
	private final int originalUserRotation;
	
	public PassiveOrientationChanger(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
		
		this.originalUserRotation = Settings.System.getInt(this.contentResolver, Settings.System.USER_ROTATION, Surface.ROTATION_0);
	}
	
	@Override
	public void set(Orientation value) {
		int surfaceRotation = value.androidSurfaceRotation();
		boolean result = Settings.System.putInt(this.contentResolver, Settings.System.USER_ROTATION, surfaceRotation);
		if (!result)
			throw new RuntimeException("Couldn't change orientation");
	}

	@Override
	public void close() {
		boolean result = Settings.System.putInt(this.contentResolver, Settings.System.USER_ROTATION, this.originalUserRotation);
		if (!result)
			throw new RuntimeException("Couldn't change orientation");
	}

	@Override
	public Orientation getOriginal() {
		return Orientation.fromAndroidSurfaceRotation(this.originalUserRotation);
	}
}
