package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

public interface Mode extends Parcelable {
	@DrawableRes
	int notificationIcon();
	
	@DrawableRes
	int actionIcon();
	
	@DrawableRes
	int dashboardIcon();
	
	@StringRes
	int autoRotationStateResourceId();
	
	@StringRes
	int toggleToOtherModeResourceId();
	
	boolean equals(Mode other);
}
