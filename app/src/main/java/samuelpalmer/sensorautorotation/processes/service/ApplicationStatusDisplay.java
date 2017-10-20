package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.values.ApplicationStatus;

public interface ApplicationStatusDisplay {

	void onApplicationStopped();
	void update(ApplicationStatus applicationStatus);

}
