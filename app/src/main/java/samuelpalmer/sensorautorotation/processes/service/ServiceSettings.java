package samuelpalmer.sensorautorotation.processes.service;

import android.content.Context;
import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

/**
 * Holds persistent service state. Not intended for backup or sync.
 * Only for use within the service process.
 *
 * This is intentionally kept simple so that {@link SharedPreferences} access is consistent with
 * other apps.
 */
public abstract class ServiceSettings {
	
	public static SharedPreferences getSharedPreferences(Context context) {
		if (!CurrentProcess.isServiceProcess(context))
			throw new RuntimeException("Service settings accessed outside of the service process");
		
		return context.getSharedPreferences("service", Context.MODE_PRIVATE);
	}
	
	// NOTE: If you change preference config, add a migration to the "Migrations" class
	
	/**
	 * Has no value if the service is off. Otherwise set to the side the user is on.
	 */
	public static final String KEY_PERSISTENT_STATE = "persistentState";
	
	/**
	 * The error, if any, that occurred when the service was last running.
	 */
	public static final String KEY_LAST_ERROR_JSON = "lastError";
	
	/**
	 * Used for upgrading settings data when upgrading to a newer version of the app.
	 */
	public static final String KEY_APP_VERSION = "appVersion";
	
	/**
	 * <b>Cached copy</b> of {@link ApplicationSettings#KEY_INCOMPATIBLE_APP_PACKAGES} for use in the service process.
	 */
	public static final String KEY_INCOMPATIBLE_APP_PACKAGES = "incompatibleAppPackages";

	/**
	 * <b>Cached copy</b> of {@link ApplicationSettings#KEY_UNINSTALLING_TIP_SHOWN} for use in the service process.
	 */
	public static final String KEY_UNINSTALLING_TIP_SHOWN = "uninstallingTipShown";

}
