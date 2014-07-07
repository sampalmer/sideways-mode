package samuelpalmer.sensorautorotation;

import java.util.Arrays;
import java.util.List;
import android.content.pm.ActivityInfo;

public class Orientation {
	
	private final int androidScreenOrientation;

	public Orientation(int androidDeviceOrientation) {
		this.androidScreenOrientation = androidDeviceOrientation;
		if (clockwiseIndex() == -1)
			throw new RuntimeException("Unknown orientation: " + androidDeviceOrientation);
	}

	public boolean isPortrait() {
		return this.androidScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
				|| this.androidScreenOrientation == ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
	}
	
	public int android() {
		return this.androidScreenOrientation;
	}
	
	public Rotation minus(Orientation other) {
		return new Rotation(clockwiseIndex() - other.clockwiseIndex());
	}

	public Orientation plus(Rotation change) {
		int unwrappedResultingClockwiseIndex = clockwiseIndex() + change.clockwiseCount();
		int wrappedResultingClockwiseIndex = IntegerUtilities.wrap(unwrappedResultingClockwiseIndex, offsetToAndroidOrientation.size());
		int androidDeviceOrientation = offsetToAndroidOrientation.get(wrappedResultingClockwiseIndex);
		
		return new Orientation(androidDeviceOrientation);
	}

	private int clockwiseIndex() {
		return offsetToAndroidOrientation.indexOf(this.androidScreenOrientation);
	}
	
	@Override
	public boolean equals(Object o) {
		return o != null
				&& this.getClass() == o.getClass()
				&& this.androidScreenOrientation == ((Orientation)o).androidScreenOrientation;
	}

	private static final List<Integer> offsetToAndroidOrientation = Arrays.asList(
		ActivityInfo.SCREEN_ORIENTATION_PORTRAIT,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
		ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT,
		ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
	);
}
