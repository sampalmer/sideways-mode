package samuelpalmer.sensorautorotation.notifications.service;

public interface Mode {
	public int iconResourceId(boolean overridden);
	public int summaryResourceId();
	public int actionDescriptionResourceId();
}
