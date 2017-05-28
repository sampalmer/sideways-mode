package samuelpalmer.sensorautorotation.notifications.service;

public interface ApplicationStatusDisplay {

	void onApplicationStopped();
	void update(ApplicationStatus applicationStatus);

}
