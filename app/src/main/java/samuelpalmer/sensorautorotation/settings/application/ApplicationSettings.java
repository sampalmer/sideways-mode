package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.common.contentprovidersharedpreferences.ContentProviderSharedPreferences;
import samuelpalmer.common.settings.application.BooleanSetting;
import samuelpalmer.common.settings.application.EnumSetting;
import samuelpalmer.common.settings.application.IntegerSetting;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.common.settings.application.StringSetSetting;
import samuelpalmer.common.settings.application.StringSetting;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

public class ApplicationSettings extends Settings {

	public static String sharedPreferencesName() {
		return CurrentProcess.context().getPackageName() + "_preferences";
	}

	private static ApplicationSettings remote;

	/**
	 * For use outside the service process.
	 */
	public static ApplicationSettings remote() {
		if (remote == null)
			remote = new ApplicationSettings(new ContentProviderSharedPreferences(CurrentProcess.context(), StringResource.get(R.string.preferences_provider_authority).toString(), sharedPreferencesName()));
		return remote;
	}

	private static ApplicationSettings local;

	/**
	 * For use in the service process for improved performance.
	 */
	public static ApplicationSettings local() {
		if (local == null)
			local = new ApplicationSettings(CurrentProcess.context().getSharedPreferences(sharedPreferencesName(), 0));
		return local;
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

	public Setting<Boolean> enabled() { return new Setting<>(new BooleanSetting(), "enabled", false); }
	public Setting<Boolean> allowUpsideDownScreen() { return new Setting<>(new BooleanSetting(), "allowUpsideDownScreen", true); }
	public Setting<NotificationPriority> notificationPriority() { return new Setting<>(new EnumSetting<>(NotificationPriority.class), "notificationPriority", NotificationPriority.NORMAL); }
	public Setting<Set<String>> forcedOrientationAppPackages() { return new Setting<>(new StringSetSetting(), "forcedOrientationAppPackages", new HashSet<String>(0)); }
	
	/**
	 * Not sure if this really belongs here, but having it here allows us to listen for changes to
	 * the error.
	 *
	 * Maybe this could be moved to a file in the app's cache directory?
	 */
	public Setting<String> lastErrorJson() { return new Setting<>(new StringSetting(), "lastError", null); }

}
