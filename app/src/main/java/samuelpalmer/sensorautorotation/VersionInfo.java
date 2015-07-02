package samuelpalmer.sensorautorotation;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public abstract class VersionInfo {

	public static int installedVersionCode() {
		return packageInfo().versionCode;
	}

	public static PackageInfo packageInfo() {
		try {
			return Process.context().getPackageManager().getPackageInfo(Process.context().getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
