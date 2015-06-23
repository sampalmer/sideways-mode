package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceDisabler extends ServiceReceiver {
	public static void sendDisableRequest() {
		Process.context().sendBroadcast(new Intents(ServiceDisabler.class).makeIntent());
	}

	@Override
	protected void onReceive() {
		new ApplicationSettings(Prefs.prefs()).enabled().setAndCommit(false);
	}

}
