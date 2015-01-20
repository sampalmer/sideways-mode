package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class VersionInfo {

	private final Context context;

	public VersionInfo(Context context) {

		this.context = context;
	}

	public int installedVersionCode() {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
