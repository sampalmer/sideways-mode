package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.ServiceController;

abstract class ServiceProcess {

	/**
	 * Must be called at all entry points to this process
	 */
	public static void ensureInitialised() {
		//Since the service might have stopped unexpectedly without this process being killed.
		//For example, the user might've explicitly stopped the service in the Android app manager.
		ServiceController.updateFromSettings();
	}

}
