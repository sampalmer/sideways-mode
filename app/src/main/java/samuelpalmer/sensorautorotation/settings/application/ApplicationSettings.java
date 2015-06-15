package samuelpalmer.sensorautorotation.settings.application;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ApplicationSettings {

	public final SharedPreferences prefs;

	public ApplicationSettings(SharedPreferences prefs) {
		this.prefs = prefs;
	}

	public static String sharedPreferencesName() {
		return Process.context().getPackageName() + "_preferences";
	}

	public Setting<Integer> lastVersionUserInstructedAbout() { return new Setting<>(new IntegerSetting(), "lastVersionUserInstructedAbout", null); }
	public Setting<Boolean> autoRotate() { return new Setting<>(new BooleanSetting(), "autoRotate", null); }
	public Setting<Integer> userOrientation() { return new Setting<>(new IntegerSetting(), "userOrientation", null); }

	public Setting<Boolean> enabled() { return new Setting<>(new BooleanSetting(), "enabled", true); }
	public Setting<Boolean> allowUpsideDownScreen() { return new Setting<>(new BooleanSetting(), "allowUpsideDownScreen", false); }
	public Setting<ScreenOffBehaviour> screenOffBehaviour() { return new Setting<>(new EnumSetting<>(ScreenOffBehaviour.class), "screenOnBehaviour", ScreenOffBehaviour.DO_NOTHING); }
	public Setting<NotificationPriority> notificationPriority() { return new Setting<>(new EnumSetting<>(NotificationPriority.class), "notificationPriority", NotificationPriority.NORMAL); }

	public class Setting<T> {

		private final EventRegistrar onChange = new UpdatingEventRegistrar() {

			private final SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
				@Override
				public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
					if (key.equals(Setting.this.key))
						report();
				}
			};

			@Override
			public void subscribe(EventHandler subscriber) {
				if (noSubscriptions())
					prefs.registerOnSharedPreferenceChangeListener(listener);
				super.subscribe(subscriber);
			}

			@Override
			public void unsubscribe(EventHandler subscriber) {
				super.unsubscribe(subscriber);
				if (noSubscriptions())
					prefs.unregisterOnSharedPreferenceChangeListener(listener);
			}

		};

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
			return getRaw() != null;
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
			type.put(transaction.editor, key, value);
		}

		public void remove(Transaction transaction) {
			transaction.editor.remove(key);
		}

		public String getKey() {
			return key;
		}

		public EventRegistrar onChange() {
			return onChange;
		}

	}

	public class Transaction {
		private final Editor editor;

		@SuppressLint("CommitPrefEdits")
		public Transaction() {
			editor = prefs.edit();
		}

		public void commit() {
			if (!editor.commit())
				// Since a look over the implementation of Editor.commit() indicates this represents a file system or other IO error.
				EnvironmentError.report(R.string.preference_write_failure_message);
		}
	}

}
