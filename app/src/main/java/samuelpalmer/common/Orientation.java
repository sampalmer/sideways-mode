package samuelpalmer.common;

import android.support.annotation.NonNull;

public enum Orientation { // Using an enum to reduce heap allocation and garbage collection
	
	NATURAL(0, "Natural"),
	RIGHT_SIDE(1, "Right Side"),
	UPSIDE_DOWN(2, "Upside-Down"),
	LEFT_SIDE(3, "Left Side");
	
	private static final int numberOfPossibleRotations = 4;
	
	/**
	 * Number of clockwise 90 degree turns from natural/upright orientation. Must be in the range [0, 3].
	 */
	public final int offset;
	private final String name;
	
	Orientation(int offset, String name) {
		this.offset = offset;
		this.name = name;
	}
	
	/**
	 * @param offset Number of clockwise 90 degree turns from natural/upright orientation. Must be in the range [0, 3].
	 */
	public static Orientation fromOffset(int offset) throws IllegalArgumentException {
		switch (offset) {
			case 0: return Orientation.NATURAL;
			case 1: return Orientation.RIGHT_SIDE;
			case 2: return Orientation.UPSIDE_DOWN;
			case 3: return Orientation.LEFT_SIDE;
			default: throw new IllegalArgumentException("Unknown orientation: " + offset);
		}
	}
	
	public Orientation minus(@NonNull Orientation other) {
		return fromOffset(NumberUtilities.wrap(offset - other.offset, numberOfPossibleRotations));
	}

	public Orientation plus(@NonNull Orientation change) {
		return fromOffset(NumberUtilities.wrap(offset + change.offset, numberOfPossibleRotations));
	}
	
	@Override
	public String toString() {
		return name;
	}
	
}
