package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.ContentProviderSharedPreferences;

public class ApplicationSettings extends Settings {

	public static String sharedPreferencesName() {
		return Process.context().getPackageName() + "_preferences";
	}

	private static ApplicationSettings remote;

	/**
	 * For use outside the service process.
	 */
	public static ApplicationSettings remote() {
		if (remote == null)
			remote = new ApplicationSettings(new ContentProviderSharedPreferences(sharedPreferencesName()));
		return remote;
	}

	private static ApplicationSettings local;

	/**
	 * For use in the service process for improved performance.
	 */
	public static ApplicationSettings local() {
		if (local == null)
			local = new ApplicationSettings(Process.context().getSharedPreferences(sharedPreferencesName(), 0));
		return local;
	}

	private ApplicationSettings(SharedPreferences sharedPreferences) {
		super(sharedPreferences);
	}

	public Setting<Integer> lastVersionUserInstructedAbout() { return new Setting<>(new IntegerSetting(), "lastVersionUserInstructedAbout", null); }

	public Setting<Boolean> enabled() { return new Setting<>(new BooleanSetting(), "enabled", false); }
	public Setting<Boolean> allowUpsideDownScreen() { return new Setting<>(new BooleanSetting(), "allowUpsideDownScreen", true); }
	public Setting<NotificationPriority> notificationPriority() { return new Setting<>(new EnumSetting<>(NotificationPriority.class), "notificationPriority", NotificationPriority.NORMAL); }

}
