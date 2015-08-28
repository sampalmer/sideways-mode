package samuelpalmer.sensorautorotation.settings.application;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.settings.application.multiprocess.MultiProcessSharedPreferences;

public class ApplicationSettings extends Settings {

	private static ApplicationSettings instance;

	public static ApplicationSettings instance() {
		if (instance == null)
			instance = new ApplicationSettings();
		return instance;
	}

	public static String sharedPreferencesName() {
		return Process.context().getPackageName() + "_preferences";
	}

	private ApplicationSettings() {
		super(new MultiProcessSharedPreferences(sharedPreferencesName()));
	}

	public Setting<Integer> lastVersionUserInstructedAbout() { return new Setting<>(new IntegerSetting(), "lastVersionUserInstructedAbout", null); }

	public Setting<Boolean> enabled() { return new Setting<>(new BooleanSetting(), "enabled", false); }
	public Setting<Boolean> allowUpsideDownScreen() { return new Setting<>(new BooleanSetting(), "allowUpsideDownScreen", true); }
	public Setting<NotificationPriority> notificationPriority() { return new Setting<>(new EnumSetting<>(NotificationPriority.class), "notificationPriority", NotificationPriority.NORMAL); }

}
