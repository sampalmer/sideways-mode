package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Settings {

	private static final String FILE_NAME = "samuelpalmer.sensorautorotation.prefs";
	private final SharedPreferences prefs;

	public Settings(Context context) {
		prefs = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
	}

	public Setting<Boolean> enabled() { return new BooleanSetting("enabled"); }

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

}
