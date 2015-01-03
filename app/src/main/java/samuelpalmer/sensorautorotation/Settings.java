package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class Settings {

	private final SharedPreferences prefs;
	private SharedPreferences.OnSharedPreferenceChangeListener listener;

	public Settings(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public Setting<Boolean> enabled() { return new BooleanSetting("enabled"); }
	public Setting<Boolean> allowUpsideDownScreen() { return new BooleanSetting("allowUpsideDownScreen"); }

	public void listen(final Listener listener) {
		if (this.listener != null)
			throw new RuntimeException("Already listening");

		this.listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				listener.tell();
			}
		};

		prefs.registerOnSharedPreferenceChangeListener(this.listener);
	}

	public void stopListening() {
		prefs.unregisterOnSharedPreferenceChangeListener(listener);
	}

	public interface Listener {
		void tell();
	}

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
