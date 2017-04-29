package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;

import samuelpalmer.common.IntegerUtilities;
import samuelpalmer.common.Orientation;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class UserOrientation implements Parcelable {
	
	@Nullable
	private final Orientation user;
	private final int rotations;

	public UserOrientation(@Nullable Orientation user, ScreenRotationMonitor screenRotationMonitor, @Nullable Orientation proposed) {
		this.user = user;
		rotations = proposed == null ? -1 : IntegerUtilities.wrap(screenRotationMonitor.currentRotation().minus(proposed).clockwiseCount(), 4);
	}

	private UserOrientation(Parcel in) {
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
		int longDescription = longDescriptionId();
		if (longDescription == R.string.user_rotation_left_side || longDescription == R.string.user_rotation_right_side)
			return R.string.user_rotation_sideways;

		return longDescription;
	}

	public int longDescriptionId() {
		if (user == null)
			return R.string.user_rotation_unknown;
			
		switch (user.getClockwiseRotationsFromNaturalOrientation()) {
			case 0: return R.string.user_rotation_upright;
			case 1: return R.string.user_rotation_right_side;
			case 2: return R.string.user_rotation_upside_down;
			case 3: return R.string.user_rotation_left_side;
			default: throw new RuntimeException("Unknown user rotation: " + user.getClockwiseRotationsFromNaturalOrientation());
		}
	}

	@DrawableRes
	public int iconId() {
		switch (rotations) {
			case -1: return 0;
			case 0: return R.drawable.notification_user_upright;
			case 1: return R.drawable.notification_user_left_side;
			case 2: return R.drawable.notification_user_upside_down;
			case 3: return R.drawable.notification_user_right_side;
			default: throw new RuntimeException("Unknown user rotation: " + rotations);
		}
	}

	public boolean equals(UserOrientation other) {
		return
			(user == other.user || user != null && user.equals(other.user))
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
