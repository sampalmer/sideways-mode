package samuelpalmer.sensorautorotation;

import android.annotation.SuppressLint;
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
		PreferenceManager.setDefaultValues(context, R.xml.preferences, false);
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public Setting<Integer> lastVersionUserInstructedAbout() { return new IntegerSetting("lastVersionUserInstructedAbout"); }
	public Setting<Boolean> autoRotate() { return new BooleanSetting("autoRotate"); }
	public Setting<Integer> userOrientation() { return new IntegerSetting("userOrientation"); }

	public Setting<Boolean> enabled() { return new BooleanSetting("enabled"); }
	public Setting<Boolean> allowUpsideDownScreen() { return new BooleanSetting("allowUpsideDownScreen"); }
	public Setting<ScreenOnBehaviour> screenOnBehaviour() { return new EnumSetting<>("screenOnBehaviour", ScreenOnBehaviour.class); }

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

		@SuppressWarnings("unchecked")
		protected T convertFromRaw(Object raw) {
			return (T)raw;
		}

		private T tryGet() {
			Object raw = prefs.getAll().get(key);
			return convertFromRaw(raw);
		}

		public T get() {
			T value = tryGet();
			if (value == null)
				throw new RuntimeException("No preference value found for key " + key);

			return value;
		}

		public T getOr(T defaultValue) {
			T value = tryGet();
			if (value == null)
				value = defaultValue;

			return value;
		}

		public boolean hasValue() {
			return tryGet() != null;
		}

		public void setAndCommit(T value) {
			Transaction transaction = new Transaction();
			set(value, transaction);
			transaction.commit();
		}

		public void set(T value, Transaction transaction) {
			put(transaction.editor, value);
		}

		public void remove(Transaction transaction) {
			transaction.editor.remove(key);
		}

		public String getKey() {
			return key;
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
		protected void put(Editor editor, Boolean value) {
			editor.putBoolean(key, value);
		}
	}

	private class IntegerSetting extends Setting<Integer> {

		public IntegerSetting(String key) {
			super(key);
		}

		@Override
		protected void put(Editor editor, Integer value) {
			editor.putInt(key, value);
		}

	}

	private class EnumSetting<T extends Enum<T>> extends Setting<T> {

		private final Class<T> clazz;

		public EnumSetting(String key, Class<T> clazz) {
			super(key);
			this.clazz = clazz;
		}

		@Override
		protected void put(Editor editor, T value) {
			editor.putString(key, value.name());
		}

		@Override
		protected T convertFromRaw(Object raw) {
			if (raw == null)
				return null;

			return Enum.valueOf(clazz, (String)raw);
		}
	}

	public enum ScreenOnBehaviour {
		DO_NOTHING,
		LOCK_SCREEN_ROTATION
	}

	public class Transaction {
		private final Editor editor;

		@SuppressLint("CommitPrefEdits")
		public Transaction() {
			editor = prefs.edit();
		}

		public void commit() {
			if (!editor.commit())
				throw new RuntimeException("Failed to write settings");
		}
	}

}
