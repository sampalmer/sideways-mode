package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.settings.application.IntegerSetting;
import samuelpalmer.common.settings.application.StringSetSetting;
import samuelpalmer.common.settings.application.StringSetting;
import samuelpalmer.sensorautorotation.CurrentProcess;

public class ApplicationSettings extends ProcessIndependentSettings {

	public static String sharedPreferencesName() {
		return CurrentProcess.context().getPackageName() + "_preferences";
	}

	private static ApplicationSettings instance;
	
	public static ApplicationSettings instance() {
		if (instance == null)
			instance = new ApplicationSettings(makePreferences(sharedPreferencesName()));
		
		return instance;
	}
	
	public final SharedPreferences sharedPreferences;

	private ApplicationSettings(SharedPreferences sharedPreferences) {
		super(sharedPreferences);
		this.sharedPreferences = sharedPreferences;
	}
	
	//NOTE: If you add/remove/change preference config, add a migration to the "Migrations" class
	
	/**
	 * Serves 2 purposes:
	 * - Version of stored settings
	 * - Whether the user has viewed the startup instructions screen
	 */
	public Setting<Integer> lastVersionUserInstructedAbout() { return new Setting<>(new IntegerSetting(), "lastVersionUserInstructedAbout", null); }
	
	public Setting<Set<String>> forcedOrientationAppPackages() { return new Setting<>(new StringSetSetting(), "forcedOrientationAppPackages", new HashSet<String>(0)); }
	
}
