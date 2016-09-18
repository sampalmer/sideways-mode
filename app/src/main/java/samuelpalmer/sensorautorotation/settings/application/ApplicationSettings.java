package samuelpalmer.sensorautorotation.settings.application;

import android.content.SharedPreferences;

import samuelpalmer.common.contentprovidersharedpreferences.ContentProviderSharedPreferences;
import samuelpalmer.common.settings.application.BooleanSetting;
import samuelpalmer.common.settings.application.EnumSetting;
import samuelpalmer.common.settings.application.IntegerSetting;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;

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
			remote = new ApplicationSettings(new ContentProviderSharedPreferences(Process.context(), StringResource.get(R.string.preferences_provider_authority).toString(), sharedPreferencesName()));
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

	public final SharedPreferences sharedPreferences;

	private ApplicationSettings(SharedPreferences sharedPreferences) {
		super(sharedPreferences);
		this.sharedPreferences = sharedPreferences;
	}

	public Setting<Integer> lastVersionUserInstructedAbout() { return new Setting<>(new IntegerSetting(), "lastVersionUserInstructedAbout", null); }

	public Setting<Boolean> enabled() { return new Setting<>(new BooleanSetting(), "enabled", false); }
	public Setting<Boolean> allowUpsideDownScreen() { return new Setting<>(new BooleanSetting(), "allowUpsideDownScreen", true); }
	public Setting<NotificationPriority> notificationPriority() { return new Setting<>(new EnumSetting<>(NotificationPriority.class), "notificationPriority", NotificationPriority.NORMAL); }
	public Setting<Boolean> inferUserWhenLocked() { return new Setting<>(new BooleanSetting(), "inferUserWhenDeviceLocked", false); }

}
