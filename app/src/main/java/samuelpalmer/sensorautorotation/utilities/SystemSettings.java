package samuelpalmer.sensorautorotation.utilities;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public abstract class SystemSettings {
	
	public static boolean canWrite(Context context) {
		return Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(context);
	}
	
}
