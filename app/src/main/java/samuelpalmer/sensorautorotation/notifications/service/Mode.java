package samuelpalmer.sensorautorotation.notifications.service;

public interface Mode {
	int notificationIcon();
	int actionIcon();
	int autoRotationStateResourceId();
	int toggleToOtherModeResourceId();
	boolean equals(Mode other);
}
