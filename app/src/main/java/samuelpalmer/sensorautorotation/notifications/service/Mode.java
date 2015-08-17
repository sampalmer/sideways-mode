package samuelpalmer.sensorautorotation.notifications.service;

public interface Mode {
	int iconResourceId(boolean overridden);
	String iconSvgFileName();
	int autoRotationStateResourceId();
}
