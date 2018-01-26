package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;

class InstalledApp {

	public final String packageName;
	public final CharSequence label;
	private final ApplicationInfo app;

	public InstalledApp(ApplicationInfo app, PackageManager packageManager) {
		packageName = app.packageName;
		label = app.loadLabel(packageManager);
		this.app = app;
	}
	
	public Drawable loadIcon(PackageManager packageManager) {
		// The icon must be loaded dynamically as needed to so we don't run out of RAM.
		// A user with a Galaxy On Nxt actually ran out of RAM once due to all the icons being preloaded.
		return app.loadIcon(packageManager);
	}

}
