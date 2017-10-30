package samuelpalmer.sensorautorotation.upgrade;

import android.content.Context;
import android.content.SharedPreferences;

class Migration {
	
	/**
	 * Upgrades the UI process's data. This is called from within the UI process, so it must not access the service process's data.
	 */
	public void upgradeUiProcessData(Context context, SharedPreferences applicationSettings) {}
	
	/**
	 * Upgrades the service process's data. This is called from within the service process, so it must not access the UI process's data.
	 */
	public void upgradeServiceProcessData(Context context, SharedPreferences serviceSettings) {}
	
}
