package samuelpalmer.sensorautorotation.upgrade;

import android.content.SharedPreferences;

class Migration {
	
	/**
	 * Upgrades the UI process's data. This is called from within the UI process, so it must not access the service process's data.
	 */
	public void upgradeUiProcessData(SharedPreferences applicationSettings) {}
	
	/**
	 * Upgrades the service process's data. This is called from within the service process, so it must not access the UI process's data.
	 */
	public void upgradeServiceProcessData(SharedPreferences serviceSettings) {}
	
	/**
	 * Called in the UI process immediately before {@link #upgradeUiProcessData(SharedPreferences)}.
	 * Return {@code null} if no message is applicable.
	 */
	public CharSequence getMessage(SharedPreferences applicationSettings) {
		return null;
	}
	
}
