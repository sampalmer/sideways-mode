package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;

import samuelpalmer.common.IntegerUtilities;
import samuelpalmer.common.Orientation;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.processes.service.screen.ScreenRotationMonitor;

public class UserOrientation implements Parcelable {
	
	@NonNull
	private final UserSide side;
	private final int rotationsRelativeToScreen;

	public UserOrientation(@NonNull UserSide side, ScreenRotationMonitor screenRotationMonitor, @NonNull Orientation proposed) {
		this.side = side;
		rotationsRelativeToScreen = IntegerUtilities.wrap(screenRotationMonitor.currentRotation().minus(proposed).clockwiseCount(), 4);
	}

	private UserOrientation(Parcel in) {
		side = UserSide.valueOf(in.readString());
		rotationsRelativeToScreen = in.readInt();
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

	@StringRes
	public int descriptionId() {
		return side.descriptionId();
	}

	@DrawableRes
	public int iconId() {
		switch (rotationsRelativeToScreen) {
			case 0: return R.drawable.notification_user_upright;
			case 1: return R.drawable.notification_user_left_side;
			case 2: return R.drawable.notification_user_upside_down;
			case 3: return R.drawable.notification_user_right_side;
			default: throw new RuntimeException("Unknown user rotation: " + rotationsRelativeToScreen);
		}
	}

	public boolean equals(UserOrientation other) {
		return
			side.equals(other.side)
			&&
			rotationsRelativeToScreen == other.rotationsRelativeToScreen;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(side.name());
		dest.writeInt(rotationsRelativeToScreen);
	}
}
