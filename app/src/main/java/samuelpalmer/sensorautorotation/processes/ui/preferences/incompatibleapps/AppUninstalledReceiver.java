package samuelpalmer.sensorautorotation.processes.ui.preferences.incompatibleapps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

/**
 * Detects when a third-party app is uninstalled and removes it from the list of incompatible apps.
 * This is important since uninstalling an app on Android typically undoes everything related to the app.
 * Also, it's confusing to the user if an app that isn't installed shows up in the list.
 */
public class AppUninstalledReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(AppUninstalledReceiver.class.getSimpleName(), "Received intent: " + intent.getAction());
		
		if (!Intent.ACTION_PACKAGE_FULLY_REMOVED.equals(intent.getAction())) {
			Log.w(AppUninstalledReceiver.class.getSimpleName(), "Unknown action: " + intent.getAction());
			return;
		}
		
		Uri intentData = intent.getData();
		if (intentData == null) {
			Log.w(AppUninstalledReceiver.class.getSimpleName(), "Intent had no data");
			return;
		}
		
		String scheme = intentData.getScheme();
		if (scheme == null || !scheme.equals("package")) {
			Log.w(AppUninstalledReceiver.class.getSimpleName(), "Intent data URI scheme was not 'package', but instead: " + scheme);
			return;
		}
		
		String packageName = intentData.getSchemeSpecificPart();
		if (packageName == null) {
			Log.w(AppUninstalledReceiver.class.getSimpleName(), "Package name was null");
			return;
		}
		
		SharedPreferences applicationSettings = ApplicationSettings.getSharedPreferences(context);
		
		// Cloning the set to prevent bug https://stackoverflow.com/q/9803838/238753.
		Set<String> incompatibleAppPackages = new HashSet<>(applicationSettings.getStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet()));
		boolean wasInList = incompatibleAppPackages.remove(packageName);
		if (wasInList) {
			Log.i(AppUninstalledReceiver.class.getSimpleName(), "An incompatible app was uninstalled. Removing from the list.");
			
			applicationSettings
				.edit()
				.putStringSet(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES, incompatibleAppPackages)
				.apply();
		}
	}
	
}
