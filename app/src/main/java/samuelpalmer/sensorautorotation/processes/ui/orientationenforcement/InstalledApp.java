package samuelpalmer.sensorautorotation.processes.ui.orientationenforcement;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

class InstalledApp {

	public final CharSequence packageName;
	public final CharSequence label;
	public final Drawable icon;

	public InstalledApp(PackageInfo app, PackageManager packageManager) {
		packageName = app.packageName;
		label = app.applicationInfo.loadLabel(packageManager);
		icon = app.applicationInfo.loadIcon(packageManager); //TODO: Maybe it's bad for memory to load all the icons into memory at once
	}

}
