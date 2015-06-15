package samuelpalmer.sensorautorotation.processes.service;

import android.app.Service;

abstract class ServiceService extends Service {

	@Override
	public void onCreate() {
		super.onCreate();
		ServiceProcess.ensureInitialised();
	}
}
