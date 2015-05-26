package samuelpalmer.sensorautorotation.settings.application;

import java.lang.reflect.Field;
import java.util.ArrayList;

import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings.Setting;

/**
 * We're using this instead of {@link android.preference.PreferenceManager#setDefaultValues} since
 * that doesn't actually set the default values when they're "false" on Android 4.0.4.
 */
public class SettingsInitialiser {

	private final ApplicationSettings source;
	private final ArrayList<Setting<?>> settings = new ArrayList<>();

	public SettingsInitialiser(ApplicationSettings source) {
		this.source = source;

		for (Field field : fields())
			new Candidate(field).consider();

		if (settings.isEmpty())
			throw new RuntimeException("Couldn't identify application settings.");
	}

	private Field[] fields() {
		return source.getClass().getDeclaredFields();
	}

	public void ensureInitialised() {
		for (Setting<?> setting : settings)
			setting.ensureInitialised();
	}

	public class Candidate {
		private final Field member;

		public Candidate(Field member) {
			this.member = member;
		}

		public void consider() {
			if (isSetting())
				settings.add(getSetting());
		}

		private boolean isSetting() {
			return member.getType() == Setting.class;
		}

		private Setting<?> getSetting() {
			try {
				return (Setting<?>) member.get(source);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
	}
}
