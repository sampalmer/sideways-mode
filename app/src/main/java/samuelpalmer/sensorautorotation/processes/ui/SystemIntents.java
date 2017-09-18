package samuelpalmer.sensorautorotation.processes.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.RequiresApi;

public class SystemIntents {
	
	private final Context context;
	
	public SystemIntents(Context context) {
		this.context = context;
	}
	
	@RequiresApi(api = Build.VERSION_CODES.M)
	public Intent modifySystemSettings() {
		return new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, packageUri());
	}
	
	public Intent uninstall() {
		return new Intent(Intent.ACTION_UNINSTALL_PACKAGE, packageUri());
	}
	
	private Uri packageUri() {
		return Uri.fromParts("package", context.getPackageName(), null);
	}
	
}
