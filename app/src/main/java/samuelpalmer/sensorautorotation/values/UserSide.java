package samuelpalmer.sensorautorotation.values;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import samuelpalmer.sensorautorotation.R;

public enum UserSide {
    
    LEFT(R.string.user_rotation_left_side, Orientation.LEFT_SIDE, R.drawable.app_left_side, R.drawable.app_left_side_filled),
    RIGHT(R.string.user_rotation_right_side, Orientation.RIGHT_SIDE, R.drawable.app_right_side, R.drawable.app_right_side_filled);
    
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
