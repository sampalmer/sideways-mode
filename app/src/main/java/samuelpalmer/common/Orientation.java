package samuelpalmer.common;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

@SuppressWarnings("unused")
public class Orientation implements Parcelable {

	private static final int numberOfPossibleRotations = 4;
	public static final Orientation NATURAL = new Orientation(0);
	public static final Orientation RIGHT_SIDE = new Orientation(1);
	public static final Orientation UPSIDE_DOWN = new Orientation(2);
	public static final Orientation LEFT_SIDE = new Orientation(3);

	private final int clockwiseRotationsFromNaturalOrientation;

	//TODO: Replace this with a factory method and reuse a single pool of objects to reduce heap allocation
	public Orientation(int clockwiseRotationsFromNaturalOrientation) throws IllegalArgumentException {
		if (clockwiseRotationsFromNaturalOrientation < 0 || clockwiseRotationsFromNaturalOrientation >= numberOfPossibleRotations)
			throw new IllegalArgumentException("Unknown orientation: " + clockwiseRotationsFromNaturalOrientation);

		this.clockwiseRotationsFromNaturalOrientation = clockwiseRotationsFromNaturalOrientation;
	}

	protected Orientation(Parcel in) {
		clockwiseRotationsFromNaturalOrientation = in.readInt();
	}

	public static final Creator<Orientation> CREATOR = new Creator<Orientation>() {
		@Override
		public Orientation createFromParcel(Parcel in) {
			return new Orientation(in);
		}

		@Override
		public Orientation[] newArray(int size) {
			return new Orientation[size];
		}
	};

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(clockwiseRotationsFromNaturalOrientation);
	}

	public Orientation negate() {
		int unwrapped = -this.clockwiseRotationsFromNaturalOrientation;
		int wrapped = NumberUtilities.wrap(unwrapped, numberOfPossibleRotations);
		return new Orientation(wrapped);
	}

	public Rotation minus(@NonNull Orientation other) {
		return new Rotation(clockwiseRotationsFromNaturalOrientation - other.clockwiseRotationsFromNaturalOrientation);
	}

	public Orientation plus(@NonNull Rotation change) {
		int unwrappedResultingClockwiseIndex = clockwiseRotationsFromNaturalOrientation + change.clockwiseCount();
		int wrappedResultingClockwiseIndex = NumberUtilities.wrap(unwrappedResultingClockwiseIndex, numberOfPossibleRotations);

		return new Orientation(wrappedResultingClockwiseIndex);
	}

	public Orientation minus(@NonNull Rotation other) {
		return plus(other.negate());
	}

	@Override
	public boolean equals(@Nullable Object o) {
		return o != null
				&& this.getClass() == o.getClass()
				&& this.clockwiseRotationsFromNaturalOrientation == ((Orientation) o).clockwiseRotationsFromNaturalOrientation;
	}

	@Override
	public String toString() {
		switch (clockwiseRotationsFromNaturalOrientation) {
			case 0:
				return "Natural";
			case 1:
				return "Right Side";
			case 2:
				return "Upside-Down";
			case 3:
				return "Left Side";
			default:
				throw new RuntimeException("Unknown number of rotations: " + clockwiseRotationsFromNaturalOrientation);
		}
	}
	
	public boolean isNaturalOrUpsideDown() {
		return clockwiseRotationsFromNaturalOrientation % 2 == 0;
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
