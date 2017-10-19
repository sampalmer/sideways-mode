package samuelpalmer.common;

/**
 * Represents the difference between two {@link Orientation}s in quarter (90 degree) turns.
 */
public class Rotation {

	private final int clockwiseRotations;

	//TODO: Replace this with a factory method and the flyweight(?) pattern?
	public Rotation(int clockwiseRotations) {
		this.clockwiseRotations = clockwiseRotations;
	}

	public int clockwiseCount() {
		return this.clockwiseRotations;
	}

	@Override
	public String toString() {
		return clockwiseRotations + " clockwise turn" + (clockwiseRotations == 1 ? "" : "s");
	}
	
}
