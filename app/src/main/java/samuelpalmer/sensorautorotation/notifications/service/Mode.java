package samuelpalmer.sensorautorotation.notifications.service;

public interface Mode {
	String iconSvgFileName(boolean overridden);
	int autoRotationStateResourceId();
}
