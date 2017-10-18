package samuelpalmer.sensorautorotation;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

import samuelpalmer.common.Orientation;

public enum UserSide {
	
	LEFT(R.string.user_rotation_left_side, Orientation.LEFT_SIDE, R.drawable.app_left_side),
	RIGHT(R.string.user_rotation_right_side, Orientation.RIGHT_SIDE, R.drawable.app_right_side);
	
	@StringRes
	public final int descriptionId;
	
	public final Orientation orientation;
	
	@DrawableRes
	public final int iconId;
	
	UserSide(@StringRes int descriptionId, Orientation orientation, @DrawableRes int iconId) {
		this.descriptionId = descriptionId;
		this.orientation = orientation;
		this.iconId = iconId;
	}
	
}
