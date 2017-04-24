package samuelpalmer.sensorautorotation.processes.directboot;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.UserManager;
import android.support.annotation.RequiresApi;
import android.util.Log;

import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.ServiceSynchroniser;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

@RequiresApi(24)
public class DirectBootReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(DirectBootReceiver.class.getSimpleName(), "Received intent: " + intent.getAction());
		
		if (!Intent.ACTION_LOCKED_BOOT_COMPLETED.equals(intent.getAction())) {
			Log.w(DirectBootReceiver.class.getSimpleName(), "Unknown action: " + intent.getAction());
			return;
		}
		
		if (!syncServiceIfUserUnlocked(context))
			ServiceController.startServiceOrCrash(context, new Intent(context, DirectBootService.class));
	}
	
	public static boolean syncServiceIfUserUnlocked(Context context) {
		boolean userUnlocked = context.getSystemService(UserManager.class).isUserUnlocked();
		if (userUnlocked)
			syncService();
		
		return userUnlocked;
	}
	
	public static void syncService() {
		new ServiceSynchroniser(ApplicationSettings.remote()).updateFromSettings();
	}
	
}
