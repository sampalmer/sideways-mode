package samuelpalmer.sensorautorotation;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import samuelpalmer.common.Rotation;

public enum UserSide {
	
	LEFT(R.string.user_rotation_left_side, new Rotation(-1), R.drawable.app_left_side, new ApplicationCommand(ApplicationCommand.ACTION_LEFT_SIDE)),
	RIGHT(R.string.user_rotation_right_side, new Rotation(1), R.drawable.app_right_side, new ApplicationCommand(ApplicationCommand.ACTION_RIGHT_SIDE));
	
	@StringRes
	public final int descriptionId;
	
	public final Rotation offset;
	
	@DrawableRes
	public final int iconId;
	
	public final ApplicationCommand command;
	
	UserSide(@StringRes int descriptionId, Rotation offset, @DrawableRes int iconId, ApplicationCommand command) {
		this.descriptionId = descriptionId;
		this.offset = offset;
		this.iconId = iconId;
		this.command = command;
	}
	
}
