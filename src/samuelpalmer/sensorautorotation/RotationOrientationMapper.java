package samuelpalmer.sensorautorotation;

import android.content.pm.ActivityInfo;
import android.view.Surface;

public class RotationOrientationMapper {
	private final int androidOrientation;
	
	public static RotationOrientationMapper fromAndroidRotation(int androidRotation) {
		int androidOrientation = orientationToRotation.getKey(androidRotation);
		return new RotationOrientationMapper(androidOrientation);
	}
	
	public static RotationOrientationMapper fromAndroidOrientation(int androidOrientation) {
		return new RotationOrientationMapper(androidOrientation);
	}
	
	private RotationOrientationMapper(int androidOrientation) {
		this.androidOrientation = androidOrientation;
	}
	
	public int getAndroidRotation() {
		return orientationToRotation.getValue(this.androidOrientation);
	}
	
	public int getAndroidOrientation() {
		return this.androidOrientation;
	}
	
	private static final BidirectionalIntMap orientationToRotation;
	
	static {
		orientationToRotation = new BidirectionalIntMap();

		orientationToRotation.add(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, Surface.ROTATION_0);
		orientationToRotation.add(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, Surface.ROTATION_90);
		orientationToRotation.add(ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT, Surface.ROTATION_180);
		orientationToRotation.add(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE, Surface.ROTATION_270);
	}
}
