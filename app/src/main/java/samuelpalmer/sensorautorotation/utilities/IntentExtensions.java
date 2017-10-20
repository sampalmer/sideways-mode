package samuelpalmer.sensorautorotation.utilities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;

public class IntentExtensions {
	
	public static boolean activityDoesntExist(Intent intent, PackageManager packageManager) {
		ComponentName appMarket = intent.resolveActivity(packageManager);
		return appMarket == null || appMarket.equals(ComponentNameExtensions.unsupportedAction);
	}
	
}
