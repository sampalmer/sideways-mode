package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class Settings {

	private final SharedPreferences prefs;

	public Settings(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public Setting<Boolean> enabled() { return new BooleanSetting("enabled"); }
	public Setting<Boolean> allowUpsideDownScreen() { return new BooleanSetting("allowUpsideDownScreen"); }

	public abstract class Setting<T> {

		private final EventRegistrar<Void> onChange = new UpdatingEventRegistrar<Void>() {

			private final SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
				@Override
				public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
					if (key.equals(Setting.this.key))
						report(null);
				}
			};

			@Override
			public void subscribe(EventHandler<Void> subscriber) {
				if (noSubscriptions())
					prefs.registerOnSharedPreferenceChangeListener(listener);
				super.subscribe(subscriber);
			}

			@Override
			public void unsubscribe(EventHandler<Void> subscriber) {
				super.unsubscribe(subscriber);
				if (noSubscriptions())
					prefs.unregisterOnSharedPreferenceChangeListener(listener);
			}

		};

		protected final String key;

		public Setting(String key) {
			this.key = key;
		}

		public abstract T get();

		public void set(T value) {
			Editor editor = prefs.edit();
			put(editor, value);
			if (!editor.commit())
				throw new RuntimeException("Failed to write settings");
		}

		public EventRegistrar<Void> onChange() {
			return onChange;
		}

		protected abstract void put(Editor editor, T value);

	}

	private class BooleanSetting extends Setting<Boolean> {
		public BooleanSetting(String key) {
			super(key);
		}

		@Override
		public Boolean get() {
			//Intentionally throws when value not present or wrong type
			Boolean value = (Boolean) prefs.getAll().get(key);
			if (value == null)
				throw new RuntimeException("No preference value found for key " + key);

			return value;
		}

		@Override
		protected void put(Editor editor, Boolean value) {
			editor.putBoolean(key, value);
		}
	}

}
