package samuelpalmer.sensorautorotation.settings.application;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import samuelpalmer.sensorautorotation.eventhandling.EventRegistrarAdaptor;

public class Settings {

	private final SharedPreferences prefs;

	protected Settings(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public class Transaction {
		private final Editor editor;

		@SuppressLint("CommitPrefEdits")
		public Transaction() {
			editor = prefs.edit();
		}

		public void commit() {
			//It'd be nice to call commit() and check the return value, but doing so introduces lag
			editor.apply();
		}

	}

	public class Setting<T> extends EventRegistrarAdaptor {

		private final SettingType<T> type;
		private final String key;
		private final T defaultValue;

		public Setting(SettingType<T> type, String key, T defaultValue) {
			this.type = type;
			this.key = key;
			this.defaultValue = defaultValue;
		}

		/**
		 * Sets this to its default value if it hasn't been set before.
		 */
		public void ensureInitialised() {
			if (!hasValue() && defaultValue != null)
				setAndCommit(defaultValue);
		}

		public T tryGet() {
			Object raw = getRaw();
			if (raw == null)
				return defaultValue;

			return type.convertFromRaw(raw);
		}

		public T get() {
			T value = tryGet();
			if (value == null)
				throw new RuntimeException("No preference value found for key " + key);

			return value;
		}

		public boolean hasValue() {
			return prefs.contains(key);
		}

		private Object getRaw() {
			return prefs.getAll().get(key);
		}

		public void setAndCommit(T value) {
			Transaction transaction = new Transaction();
			set(value, transaction);
			transaction.commit();
		}

		public void set(T value, Transaction transaction) {
			if (value == null)
				throw new IllegalArgumentException("value is null");

			type.put(transaction.editor, key, value);
		}

		public void remove(Transaction transaction) {
			transaction.editor.remove(key);
		}

		public String getKey() {
			return key;
		}

		@Override
		protected void subscribeToAdaptee() {
			prefs.registerOnSharedPreferenceChangeListener(listener);
		}

		@Override
		protected void unsubscribeFromAdaptee() {
			prefs.unregisterOnSharedPreferenceChangeListener(listener);
		}

		private final SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
			@Override
			public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
				if (key.equals(ApplicationSettings.Setting.this.key))
					report();
			}
		};

	}
}
