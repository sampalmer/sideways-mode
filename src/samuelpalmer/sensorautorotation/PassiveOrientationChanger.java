package samuelpalmer.sensorautorotation;

import android.content.ContentResolver;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

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
		int surfaceRotation = RotationOrientationMapper.fromAndroidOrientation(orientation).getAndroidRotation();
		
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

	@Override
	public int get() {
		return RotationOrientationMapper.fromAndroidRotation(this.originalRotation).getAndroidOrientation();
	}
}
