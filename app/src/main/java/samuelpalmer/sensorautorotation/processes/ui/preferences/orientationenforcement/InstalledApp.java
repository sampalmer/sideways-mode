package samuelpalmer.sensorautorotation.processes.ui.preferences.orientationenforcement;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

class InstalledApp {

	public final String packageName;
	public final CharSequence label;
	public final Drawable icon;

	public InstalledApp(ApplicationInfo app, PackageManager packageManager) {
		packageName = app.packageName;
		label = app.loadLabel(packageManager);

		//TODO: See if there's a better way to do this; don't want to run out of RAM by loading a heap of icons
		//Dynamically loading the icons as needed resulted in choppy scrolling in the list, so that doesn't seem to be the way to go...
		icon = app.loadIcon(packageManager);
	}

}
