package samuelpalmer.sensorautorotation;

import android.support.annotation.StringRes;

import samuelpalmer.common.Rotation;

public enum UserSide {
	LEFT(R.string.user_rotation_left_side, new Rotation(-1)),
	RIGHT(R.string.user_rotation_right_side, new Rotation(1));
	
	private final int descriptionId;
	private final Rotation offset;
	
	UserSide(@StringRes int descriptionId, Rotation offset) {
		this.descriptionId = descriptionId;
		this.offset = offset;
	}
	
	@StringRes
	public int descriptionId() {
		return descriptionId;
	}
	
	public Rotation offset() {
		return offset;
	}
}
