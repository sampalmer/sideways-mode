package samuelpalmer.sensorautorotation.processes.service.api;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import samuelpalmer.sensorautorotation.ApplicationCommand;
import samuelpalmer.sensorautorotation.processes.service.ApplicationInstanceManager;

public class ApplicationCommandReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		ApplicationCommand command = new ApplicationCommand(intent);
		ApplicationInstanceManager.handle(command);
	}

}
