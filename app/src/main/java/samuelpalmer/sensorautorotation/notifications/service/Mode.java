package samuelpalmer.sensorautorotation.notifications.service;

public interface Mode {
	int icon(boolean overridden);
	int autoRotationStateResourceId();
	int toggleToOtherModeAResourceId();
}
