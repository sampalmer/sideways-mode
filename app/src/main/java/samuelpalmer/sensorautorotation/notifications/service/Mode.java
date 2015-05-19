package samuelpalmer.sensorautorotation.notifications.service;

public interface Mode {
	int iconResourceId(boolean overridden);
	int summaryResourceId();
	int actionDescriptionResourceId();
}
