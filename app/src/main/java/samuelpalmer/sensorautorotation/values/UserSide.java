package samuelpalmer.sensorautorotation.values;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import samuelpalmer.sensorautorotation.R;

public enum UserSide {

    // Using filled icons for quick settings since the outlined ones look weird on Samsung and MIUI devices since their outline thicknesses differ from ours
    // Using filled icons for notification small icon since the icon is so small that outline doesn't work
    LEFT(R.string.user_rotation_left_side, Orientation.LEFT_SIDE, R.drawable.app_left_side_filled, R.drawable.app_left_side_filled),
    RIGHT(R.string.user_rotation_right_side, Orientation.RIGHT_SIDE, R.drawable.app_right_side_filled, R.drawable.app_right_side_filled);
    
    @StringRes
    public final int descriptionId;
    
    public final Orientation orientation;
    
    @DrawableRes
    public final int quickSettingIconId;
    
    @DrawableRes
    public final int notificationIconId;

    UserSide(@StringRes int descriptionId, Orientation orientation, @DrawableRes int quickSettingIconId, @DrawableRes int notificationIconId) {
        this.descriptionId = descriptionId;
        this.orientation = orientation;
        this.quickSettingIconId = quickSettingIconId;
        this.notificationIconId = notificationIconId;
    }
    
}
