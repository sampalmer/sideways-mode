package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Parcel;
import android.os.Parcelable;

import samuelpalmer.common.IntegerUtilities;
import samuelpalmer.common.Orientation;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class UserOrientation implements Parcelable {

	private final Orientation user;
	private final int rotations;

	public UserOrientation(Orientation user, ScreenRotationMonitor screenRotationMonitor, Orientation proposed) {
		this.user = user;
		rotations = IntegerUtilities.wrap(screenRotationMonitor.currentRotation().minus(proposed).clockwiseCount(), 4);
	}

	protected UserOrientation(Parcel in) {
		user = in.readParcelable(this.getClass().getClassLoader());
		rotations = in.readInt();
	}

	public static final Creator<UserOrientation> CREATOR = new Creator<UserOrientation>() {
		@Override
		public UserOrientation createFromParcel(Parcel in) {
			return new UserOrientation(in);
		}

		@Override
		public UserOrientation[] newArray(int size) {
			return new UserOrientation[size];
		}
	};

	public int descriptionId() {
		switch (user.getClockwiseRotationsFromNaturalOrientation()) {
			case 0: return R.string.user_rotation_upright;
			case 2: return R.string.user_rotation_upside_down;
			default: return R.string.user_rotation_sideways;
		}
	}

	public int iconId() {
		switch (rotations) {
			case 0: return R.drawable.user_upright;
			case 1: return R.drawable.user_left_side;
			case 2: return R.drawable.user_upside_down;
			case 3: return R.drawable.user_right_side;
			default: throw new RuntimeException("Unknown user rotation: " + rotations);
		}
	}

	public boolean equals(UserOrientation other) {
		return
			user.equals(other.user)
			&&
			rotations == other.rotations;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(user, 0);
		dest.writeInt(rotations);
	}
}
