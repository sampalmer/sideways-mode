package samuelpalmer.sensorautorotation.processes.service;

import android.content.Intent;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class ServiceDisabler extends ServiceReceiver {
	public static void sendDisableRequest() {
		Process.context().sendBroadcast(new Intent(Process.context(), ServiceDisabler.class));
	}

	@Override
	protected void onReceive() {
		new ApplicationSettings(Prefs.prefs()).enabled().setAndCommit(false);
	}

}
