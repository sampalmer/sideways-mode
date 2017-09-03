package samuelpalmer.common;

import android.support.annotation.NonNull;

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

	public Rotation negate() {
		return new Rotation(-clockwiseRotations);
	}

	public boolean isRevolution() {
		return clockwiseRotations % 4 == 0;
	}

	public Rotation plus(@NonNull Rotation other) {
		return new Rotation(clockwiseRotations + other.clockwiseRotations);
	}

	@SuppressWarnings("unused")
	public Rotation minus(@NonNull Rotation other) {
		return plus(other.negate());
	}

	@SuppressWarnings("WeakerAccess")
	public Orientation plus(@NonNull Orientation other) {
		return other.plus(this);
	}

	public Orientation minus(@NonNull Orientation other) {
		return plus(other.negate());
	}
}
