package samuelpalmer.sensorautorotation;

import android.view.Surface;


public class Orientation {
	
	private static final int numberOfPossibleRotations = 4;
	private final int clockwiseRotationsFromNaturalOrientation;

	public Orientation(int clockwiseRotationsFromNaturalOrientation) {
		if (clockwiseRotationsFromNaturalOrientation < 0 || clockwiseRotationsFromNaturalOrientation >= numberOfPossibleRotations)
			throw new RuntimeException("Unknown orientation: " + clockwiseRotationsFromNaturalOrientation);
		
		this.clockwiseRotationsFromNaturalOrientation = clockwiseRotationsFromNaturalOrientation;
	}

	public boolean isPortrait() {
		return this.clockwiseRotationsFromNaturalOrientation % 2 == 0;
	}
	
	public Rotation minus(Orientation other) {
		return new Rotation(clockwiseRotationsFromNaturalOrientation - other.clockwiseRotationsFromNaturalOrientation);
	}

	public Orientation plus(Rotation change) {
		int unwrappedResultingClockwiseIndex = clockwiseRotationsFromNaturalOrientation + change.clockwiseCount();
		int wrappedResultingClockwiseIndex = IntegerUtilities.wrap(unwrappedResultingClockwiseIndex, numberOfPossibleRotations);
		
		return new Orientation(wrappedResultingClockwiseIndex);
	}

	@Override
	public boolean equals(Object o) {
		return o != null
				&& this.getClass() == o.getClass()
				&& this.clockwiseRotationsFromNaturalOrientation == ((Orientation)o).clockwiseRotationsFromNaturalOrientation;
	}
	
	public int androidSurfaceRotation() {
		return Surface.ROTATION_0 + clockwiseRotationsFromNaturalOrientation;
	}

	public static Orientation fromAndroidSurfaceRotation(int androidSurfaceRotation) {
		return new Orientation(androidSurfaceRotation - Surface.ROTATION_0);
	}
	
}
