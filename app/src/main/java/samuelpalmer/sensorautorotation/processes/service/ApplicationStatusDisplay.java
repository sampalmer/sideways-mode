package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.ApplicationStatus;

public interface ApplicationStatusDisplay {

	void onApplicationStopped();
	void update(ApplicationStatus applicationStatus);

}
