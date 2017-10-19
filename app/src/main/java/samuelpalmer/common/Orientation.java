package samuelpalmer.common;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Orientation {

	private static final int numberOfPossibleRotations = 4;
	public static final Orientation NATURAL = new Orientation(0);
	public static final Orientation RIGHT_SIDE = new Orientation(1);
	public static final Orientation UPSIDE_DOWN = new Orientation(2);
	public static final Orientation LEFT_SIDE = new Orientation(3);
	
	/**
	 * Number of clockwise 90 degree turns from natural/upright orientation. Must be in the range [0, 3].
	 */
	public final int offset;

	//TODO: Replace this with a factory method and reuse a single pool of objects to reduce heap allocation
	
	/**
	 * @param offset Number of clockwise 90 degree turns from natural/upright orientation. Must be in the range [0, 3].
	 */
	public Orientation(int offset) throws IllegalArgumentException {
		if (offset < 0 || offset >= numberOfPossibleRotations)
			throw new IllegalArgumentException("Unknown orientation: " + offset);

		this.offset = offset;
	}
	
	public Orientation minus(@NonNull Orientation other) {
		return new Orientation(NumberUtilities.wrap(offset - other.offset, numberOfPossibleRotations));
	}

	public Orientation plus(@NonNull Orientation change) {
		return new Orientation(NumberUtilities.wrap(offset + change.offset, numberOfPossibleRotations));
	}
	
	@Override
	public boolean equals(@Nullable Object o) {
		return o != null
				&& this.getClass() == o.getClass()
				&& this.offset == ((Orientation) o).offset;
	}

	@Override
	public String toString() {
		switch (offset) {
			case 0:
				return "Natural";
			case 1:
				return "Right Side";
			case 2:
				return "Upside-Down";
			case 3:
				return "Left Side";
			default:
				throw new RuntimeException("Unknown number of rotations: " + offset);
		}
	}
	
}
