package samuelpalmer.sensorautorotation;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class VersionInfo {

	public int installedVersionCode() {
		try {
			PackageInfo packageInfo = Process.context().getPackageManager().getPackageInfo(Process.context().getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
