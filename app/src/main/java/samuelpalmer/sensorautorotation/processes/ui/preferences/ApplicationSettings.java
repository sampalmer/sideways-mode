package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.settings.application.IntegerSetting;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.common.settings.application.StringSetSetting;
import samuelpalmer.sensorautorotation.CurrentProcess;

public class ApplicationSettings extends Settings {

	public static String sharedPreferencesName() {
		return CurrentProcess.context().getPackageName() + "_preferences";
	}

	private static ApplicationSettings instance;
	
	public static ApplicationSettings instance() {
		if (instance == null) {
			if (CurrentProcess.isServiceProcess())
				throw new RuntimeException("Application settings accessed outside of the UI process");
			
			instance = new ApplicationSettings(CurrentProcess.context().getSharedPreferences(sharedPreferencesName(), 0));
		}
		
		return instance;
	}
	
	private ApplicationSettings(SharedPreferences sharedPreferences) {
		super(sharedPreferences);
	}
	
	/*
	 * NOTE: If you change preference config:
	 * 1. Add a migration to the "Migrations" class
	 * 2. Make sure your changes to the service process
	 */
	
	/**
	 * Serves 2 purposes:
	 * - Version of stored settings
	 * - Whether the user has viewed the startup instructions screen
	 */
	public Setting<Integer> lastVersionUserInstructedAbout() { return new Setting<>(new IntegerSetting(), "lastVersionUserInstructedAbout", null); }
	
	public Setting<Set<String>> forcedOrientationAppPackages() { return new Setting<>(new StringSetSetting(), "forcedOrientationAppPackages", new HashSet<String>(0)); }
	
}
