package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Activity;
import android.content.Intent;

import samuelpalmer.sensorautorotation.utilities.SystemIntents;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

public abstract class Uninstaller {
	
	public static void uninstall(Activity activity) {
		// The OS kills the app before uninstalling, so we need to gracefully shut down to reset the screen orientation.
		new ApplicationCommand(ApplicationCommand.ACTION_TURN_OFF).send(activity);
		
		Intent uninstallIntent = SystemIntents.uninstall(activity);
		activity.startActivity(uninstallIntent);
	}
	
}
