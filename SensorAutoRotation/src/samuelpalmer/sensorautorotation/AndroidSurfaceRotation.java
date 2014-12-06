package samuelpalmer.sensorautorotation;

import android.view.Surface;

public class AndroidSurfaceRotation {
	
	private final int value;

	public AndroidSurfaceRotation(int value) {
		this.value = value;
	}
	
	public AndroidSurfaceRotation(Orientation value) {
		this(Surface.ROTATION_0 + value.getClockwiseRotationsFromNaturalOrientation());
	}
	
	public Orientation orientation() throws IllegalArgumentException {
		return new Orientation(getValue() - Surface.ROTATION_0);
	}

	public int getValue() {
		return value;
	}
	
}
