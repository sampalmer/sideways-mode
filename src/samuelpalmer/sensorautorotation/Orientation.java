package samuelpalmer.sensorautorotation;

import android.content.pm.ActivityInfo;
import android.os.Parcel;
import android.os.Parcelable;

public class Orientation implements Parcelable {
	
	private final int clockwiseRotationsFromUpright;

	public static Orientation fromAndroid(int androidOrientation) {
		return new Orientation(findOffset(androidOrientation));
	}
	
	private Orientation(int unwrappedOffset) {
		this.clockwiseRotationsFromUpright = wrap(unwrappedOffset);
	}

	private int wrap(int unwrappedOffset) {
		return ((unwrappedOffset % offsetToAndroidOrientation.length) + offsetToAndroidOrientation.length) % offsetToAndroidOrientation.length;
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

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(clockwiseRotationsFromUpright);
	}
	
	public static final Parcelable.Creator<Orientation> CREATOR = new Parcelable.Creator<Orientation>() {
		@Override
		public Orientation createFromParcel(Parcel in) {
			return new Orientation(in.readInt());
		}

		@Override
		public Orientation[] newArray(int size) {
			return new Orientation[size];
		}
	};

	public int clockwiseRotationsFrom(Orientation lastDeviceOrientation) {
		return wrap(this.clockwiseRotationsFromUpright - lastDeviceOrientation.clockwiseRotationsFromUpright);
	}

	public Orientation rotatedClockwise(int rotations) {
		return new Orientation(this.clockwiseRotationsFromUpright + rotations);
	}
}
