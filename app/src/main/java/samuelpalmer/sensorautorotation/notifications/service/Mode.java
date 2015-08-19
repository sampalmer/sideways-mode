package samuelpalmer.sensorautorotation.notifications.service;

public interface Mode {
	int iconResourceId();
	String iconSvgFileName(boolean overridden);
	int autoRotationStateResourceId();
}
