package samuelpalmer.sensorautorotation.notifications.service;

public interface ServiceStatusDisplay {

	void onServiceStopped();
	void update(ServiceStatus serviceStatus);

}
