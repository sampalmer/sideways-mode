package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

/**
 * Convenience methods for creating intents for OS tasks/activities.
 */
public abstract class SystemIntents {
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	public static Intent modifySystemSettings(Context context) {
		return new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, packageUri(context));
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	public static Intent drawOnTopOfApps(Context context) {
		return new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, packageUri(context));
	}
	
	public static Intent uninstall(Context context) {
		return new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri(context));
	}
	
	private static Uri packageUri(Context context) {
		return Uri.fromParts("package", context.getPackageName(), null);
	}
	
}
