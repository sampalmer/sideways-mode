package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.Set;

public class Settings {

	private final SharedPreferences prefs;

	public Settings(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public Setting<Boolean> userInstructedAboutAccessibilityService() { return new BooleanSetting("userInstructedAboutAccessibilityService"); }
	public Setting<Boolean> enabled() { return new BooleanSetting("enabled"); }
	public Setting<Set<String>> forcedOrientationAppPackages() { return new StringSetSetting("forcedOrientationAppPackages"); }

	public abstract class Setting<T> {

		protected final String key;

		public Setting(String key) {
			this.key = key;
		}

		public abstract T getOr(T defaultValue);

		public void set(T value) {
			Editor editor = prefs.edit();
			put(editor, value);
			if (!editor.commit())
				throw new RuntimeException("Failed to write settings");
		}

		protected abstract void put(Editor editor, T value);

	}

	private class BooleanSetting extends Setting<Boolean> {
		public BooleanSetting(String key) {
			super(key);
		}

		@Override
		public Boolean getOr(Boolean defaultValue) {
			return prefs.getBoolean(key, defaultValue);
		}

		@Override
		protected void put(Editor editor, Boolean value) {
			editor.putBoolean(key, value);
		}
	}

	private class StringSetSetting extends Setting<Set<String>> {
		public StringSetSetting(String key) {
			super(key);
		}

		@Override
		public Set<String> getOr(Set<String> defaultValue) {
			return prefs.getStringSet(key, defaultValue);
		}

		@Override
		protected void put(Editor editor, Set<String> value) {
			editor.putStringSet(key, value);
		}

	}
}
