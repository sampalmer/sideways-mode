package samuelpalmer.sensorautorotation.upgrade;

import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

class Migration {
	
	/**
	 * Upgrades the UI process's data. This is called from within the UI process, so it must not access the service process's data.
	 */
	public void upgradeUiProcessData(ApplicationSettings settings) {}
	
	/**
	 * Upgrades the service process's data. This is called from within the service process, so it must not access the UI process's data.
	 */
	public void upgradeServiceProcessData(ServiceSettings settings) {}
	
	/**
	 * Called in the UI process immediately before {@link #upgradeUiProcessData(ApplicationSettings)}.
	 * Return null if no message is applicable.
	 */
	public CharSequence getMessage(ApplicationSettings settings) {
		return null;
	}
	
}
