package samuelpalmer.sensorautorotation;

/**
 * Represents the difference between two orientations; change in orientation
 */
public class Rotation {

	private final int clockwiseRotations;

	public Rotation(int clockwiseRotations) {
		this.clockwiseRotations = clockwiseRotations;
	}

	public int clockwiseCount() {
		return this.clockwiseRotations;
	}

}
