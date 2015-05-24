package samuelpalmer.sensorautorotation;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

public class VersionInfo {

	public int installedVersionCode() {
		try {
			PackageInfo packageInfo = ThisApplication.context().getPackageManager().getPackageInfo(ThisApplication.context().getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
