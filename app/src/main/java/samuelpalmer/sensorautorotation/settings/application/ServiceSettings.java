package samuelpalmer.sensorautorotation.settings.application;

import samuelpalmer.common.settings.application.EnumSetting;
import samuelpalmer.common.settings.application.StringSetting;
import samuelpalmer.sensorautorotation.UserSide;

/**
 * Holds persistent service state. Not intended for backup or sync.
 */
public class ServiceSettings extends ProcessIndependentSettings {
	
	private static ServiceSettings instance;
	
	public static ServiceSettings instance() {
		if (instance == null)
			instance = new ServiceSettings();
		
		return instance;
	}
	
	private ServiceSettings() {
		super(makePreferences("service"));
	}
	
	/**
	 * Has no value if the service is off. Otherwise set to the side the user is on.
	 */
	public Setting<UserSide> persistentState() { return new Setting<>(new EnumSetting<>(UserSide.class), "persistentState", null); }
	
	/**
	 * The error, if any, that occurred when the service was last running.
	 */
	public Setting<String> lastErrorJson() { return new Setting<>(new StringSetting(), "lastError", null); }
	
}
