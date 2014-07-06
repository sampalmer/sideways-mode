package samuelpalmer.sensorautorotation;

import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.view.Surface;

public class PassiveOrientationChanger implements OrientationChanger {

	private final ContentResolver contentResolver;
	private final int originalRotation;
	
	public PassiveOrientationChanger(ContentResolver contentResolver) {
		this.contentResolver = contentResolver;
		
		try {
			this.originalRotation = Settings.System.getInt(this.contentResolver, Settings.System.USER_ROTATION);
		} catch (SettingNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void set(int orientation) {
		int surfaceRotation;
		
		switch (orientation)
		{
			case ActivityInfo.SCREEN_ORIENTATION_PORTRAIT:
				surfaceRotation = Surface.ROTATION_0;
				break;
			case ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE:
				surfaceRotation = Surface.ROTATION_90;
				break;
			case ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT:
				surfaceRotation = Surface.ROTATION_180;
				break;
			case ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE:
				surfaceRotation = Surface.ROTATION_270;
				break;
			default:
				throw new RuntimeException("Unknown screen rotation: " + orientation);
		}
		
		setRotation(surfaceRotation);
	}

	private void setRotation(int surfaceRotation) {
		boolean result = Settings.System.putInt(this.contentResolver, Settings.System.USER_ROTATION, surfaceRotation);
		if (!result)
			throw new RuntimeException("Couldn't change orientation");
	}

	@Override
	public void close() {
		this.setRotation(this.originalRotation);
	}
}
