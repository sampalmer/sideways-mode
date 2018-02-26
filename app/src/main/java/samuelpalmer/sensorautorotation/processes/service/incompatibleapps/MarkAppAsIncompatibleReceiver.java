package samuelpalmer.sensorautorotation.processes.service.incompatibleapps;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;


public class MarkAppAsIncompatibleReceiver extends BroadcastReceiver {
	
	public static final String EXTRA_APP_PACKAGE = "app_package";
	
	public static Intent makeIntent(Context context, String appPackageName) {
		return new Intent(context, MarkAppAsIncompatibleReceiver.class).putExtra(EXTRA_APP_PACKAGE, appPackageName);
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		String appPackageName = intent.getStringExtra(EXTRA_APP_PACKAGE);
		if (appPackageName == null)
			throw new RuntimeException("No app package specified");
		
		SharedPreferences preferences = ServiceSettings.getSharedPreferences(context);
		Set<String> incompatibleAppPackages = new HashSet<>(preferences.getStringSet(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES, Collections.emptySet()));
		if (incompatibleAppPackages.add(appPackageName)) {
			preferences
				.edit()
				.putStringSet(ServiceSettings.KEY_INCOMPATIBLE_APP_PACKAGES, incompatibleAppPackages)
				.apply();
		}
	}
	
}
