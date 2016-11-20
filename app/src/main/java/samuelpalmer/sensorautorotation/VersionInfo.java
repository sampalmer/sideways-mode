package samuelpalmer.sensorautorotation;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public abstract class VersionInfo {

	public static int installedVersionCode() {
		return packageInfo().versionCode;
	}

	public static PackageInfo packageInfo() {
		try {
			return SystemServices.packageManager().getPackageInfo(CurrentProcess.context().getPackageName(), 0);
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

}
