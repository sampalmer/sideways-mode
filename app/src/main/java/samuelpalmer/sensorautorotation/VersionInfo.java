package samuelpalmer.sensorautorotation;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public abstract class VersionInfo {

	public static int installedVersionCode() {
		try {
			PackageInfo packageInfo = Process.context().getPackageManager().getPackageInfo(Process.context().getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
