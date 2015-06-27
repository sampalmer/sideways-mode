package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceDisabler extends ServiceReceiver {
	public static void sendDisableRequest() {
		Process.context().sendBroadcast(ComponentType.create(ServiceDisabler.class).intent());
	}

	@Override
	protected void onReceive() {
		new ApplicationSettings(Prefs.prefs()).enabled().setAndCommit(false);
	}

}
