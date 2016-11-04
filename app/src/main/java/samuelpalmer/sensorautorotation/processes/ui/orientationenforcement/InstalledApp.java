package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

class InstalledApp {

	public final String packageName;
	public final CharSequence label;
	private final ApplicationInfo applicationInfo;

	public InstalledApp(PackageInfo app, PackageManager packageManager) {
		packageName = app.packageName;
		label = app.applicationInfo.loadLabel(packageManager);
		applicationInfo = app.applicationInfo;
	}

	public Drawable loadIcon(PackageManager packageManager) {
		return applicationInfo.loadIcon(packageManager);
	}

}
