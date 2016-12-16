package samuelpalmer.sensorautorotation.notifications.service;

import android.os.Parcelable;

public interface Mode extends Parcelable {
	int notificationIcon();
	int actionIcon();
	int controlPanelIcon();
	int autoRotationStateResourceId();
	int toggleToOtherModeResourceId();
	boolean equals(Mode other);
}
