package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.CurrentProcess;

/**
 * The app's main {@link SharedPreferences} file. For use by the UI process only.
 * This is intentionally kept simple so that {@link SharedPreferences} access is consistent with
 * other apps.
 */
public abstract class ApplicationSettings {
	
	public static SharedPreferences getSharedPreferences(Context context) {
		if (CurrentProcess.isServiceProcess(context))
			throw new RuntimeException("Application settings accessed outside of the UI process");
		
		return context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
	}
	
	/*
	 * NOTE: If you change preference config:
	 * 1. Add a migration to the "Migrations" class
	 * 2. If you're altering a user preference, ensure the changes are synced to the service process
	 */
	
	/**
	 * Whether the user has viewed the startup instructions screen.
	 */
	public static final String KEY_USER_INSTRUCTED = "userInstructed"; // Defaults to false
	
	/**
	 * Used for upgrading settings data when upgrading to a newer version of the app.
	 */
	public static final String KEY_APP_VERSION = "appVersion";
	
	public static final String KEY_INCOMPATIBLE_APP_PACKAGES = "incompatibleAppPackages";
	
}
