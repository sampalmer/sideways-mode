package samuelpalmer.sensorautorotation;

import android.support.annotation.StringRes;

import samuelpalmer.common.Rotation;
import samuelpalmer.sensorautorotation.processes.service.api.ApplicationInstanceCommand;

public enum UserSide {
	LEFT(R.string.user_rotation_left_side, new Rotation(-1), ApplicationInstanceCommand.ACTION_SET_LEFT_SIDE),
	RIGHT(R.string.user_rotation_right_side, new Rotation(1), ApplicationInstanceCommand.ACTION_SET_RIGHT_SIDE);
	
	private final int descriptionId;
	private final Rotation offset;
	private final String action;
	
	UserSide(@StringRes int descriptionId, Rotation offset, String action) {
		this.descriptionId = descriptionId;
		this.offset = offset;
		this.action = action;
	}
	
	@StringRes
	public int descriptionId() {
		return descriptionId;
	}
	
	public Rotation offset() {
		return offset;
	}
	
	/**
	 * @see ApplicationInstanceCommand#action
	 */
	public String action() { return action; }
	
	public UserSide oppositeSide() {
		switch (this) {
			case LEFT:
				return UserSide.RIGHT;
			case RIGHT:
				return UserSide.LEFT;
			default:
				throw new RuntimeException("Can't determine opposite side of user side '" + this.toString() + "'.");
		}
	}
}
