package samuelpalmer.sensorautorotation;

import android.content.pm.ActivityInfo;

public class Orientation {
	
	private final int clockwiseRotationsFromUpright;

	public static Orientation fromAndroid(int androidOrientation) {
		int offset = findOffset(androidOrientation);
		return new Orientation(offset);
	}
	
	private Orientation(int unwrappedOffset) {
		this.clockwiseRotationsFromUpright = wrap(unwrappedOffset);
	}

	private int wrap(int unwrappedOffset) {
		int positiveWrapped = unwrappedOffset % offsetToAndroidOrientation.length;
		int nonNegative = positiveWrapped + offsetToAndroidOrientation.length;
		return nonNegative % offsetToAndroidOrientation.length;
	}
	
	public Orientation(Orientation original) {
		clockwiseRotationsFromUpright = original.clockwiseRotationsFromUpright;
	}

	public int getAndroidOrientation() {
		return offsetToAndroidOrientation[clockwiseRotationsFromUpright];
	}
	
	boolean isPortrait() {
		return clockwiseRotationsFromUpright % 2 == 0;
	}
	
	private static int findOffset(int androidOrientation) {
		for (int offset = 0; offset < offsetToAndroidOrientation.length; ++offset)
			if (offsetToAndroidOrientation[offset] == androidOrientation)
				return offset;
		
		throw new RuntimeException("Unrecognised Android orientation");
	}
	
	private static int[] offsetToAndroidOrientation = new int[] {
		ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
		ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE,
	};

	public int clockwiseRotationsFrom(Orientation lastDeviceOrientation) {
		return wrap(this.clockwiseRotationsFromUpright - lastDeviceOrientation.clockwiseRotationsFromUpright);
	}

	public Orientation rotatedClockwise(int rotations) {
		return new Orientation(this.clockwiseRotationsFromUpright + rotations);
	}
}
