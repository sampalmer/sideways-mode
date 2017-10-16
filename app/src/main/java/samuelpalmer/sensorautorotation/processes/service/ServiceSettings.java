package samuelpalmer.sensorautorotation.processes.service;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.settings.application.EnumSetting;
import samuelpalmer.common.settings.application.IntegerSetting;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.common.settings.application.StringSetSetting;
import samuelpalmer.common.settings.application.StringSetting;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;

/**
 * Holds persistent service state. Not intended for backup or sync.
 * Only for use within the service process.
 */
public class ServiceSettings extends Settings {
	
	private static ServiceSettings instance;
	
	public static ServiceSettings instance() {
		if (instance == null) {
			if (!CurrentProcess.isServiceProcess())
				throw new RuntimeException("Service settings accessed outside of the service process");
			
			instance = new ServiceSettings();
		}
		
		return instance;
	}
	
	private ServiceSettings() {
		super(CurrentProcess.context().getSharedPreferences("service", 0));
	}
	
	// NOTE: If you change preference config, add a migration to the "Migrations" class
	
	/**
	 * Has no value if the service is off. Otherwise set to the side the user is on.
	 */
	public Setting<UserSide> persistentState() { return new Setting<>(new EnumSetting<>(UserSide.class), "persistentState", null); }
	
	/**
	 * The error, if any, that occurred when the service was last running.
	 */
	public Setting<String> lastErrorJson() { return new Setting<>(new StringSetting(), "lastError", null); }
	
	/**
	 * Used for upgrading settings data when upgrading to a newer version of the app.
	 */
	public Setting<Integer> appVersion() { return new Setting<>(new IntegerSetting(), "appVersion", null); }
	
	/**
	 * Cached copy of {@link ApplicationSettings#forcedOrientationAppPackages()} for use in the service process.
	 */
	public Setting<Set<String>> forcedOrientationAppPackages() { return new Setting<>(new StringSetSetting(), "forcedOrientationAppPackages", new HashSet<String>(0)); }
	
}
