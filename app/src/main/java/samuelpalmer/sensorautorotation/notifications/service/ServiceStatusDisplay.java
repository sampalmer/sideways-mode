package samuelpalmer.sensorautorotation.notifications.service;

public abstract class ServiceStatusDisplay {

	public abstract void update(NotificationParameters params);

	@SuppressWarnings("NoopMethodInAbstractClass")
	public void cancel() {}

}
