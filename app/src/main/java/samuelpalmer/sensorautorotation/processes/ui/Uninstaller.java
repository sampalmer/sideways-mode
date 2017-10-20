package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.content.Intent;

import samuelpalmer.sensorautorotation.values.ApplicationCommand;

public abstract class Uninstaller {
	
	public static void uninstall(Activity activity) {
		new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).send(activity);
		
		Intent uninstallIntent = new SystemIntents(activity).uninstall();
		activity.startActivity(uninstallIntent);
	}
	
}
