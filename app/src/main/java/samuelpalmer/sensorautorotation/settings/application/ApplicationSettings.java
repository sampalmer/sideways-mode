package samuelpalmer.sensorautorotation.settings.application;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.EnvironmentErrorNotifier;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;

public class ApplicationSettings {

	private final SharedPreferences prefs;
	private final Context context;

	public ApplicationSettings(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public Setting<Integer> lastVersionUserInstructedAbout() { return new Setting<>(new IntegerSetting(), "lastVersionUserInstructedAbout", null); }
	public Setting<Boolean> autoRotate() { return new Setting<>(new BooleanSetting(), "autoRotate", null); }
	public Setting<Integer> userOrientation() { return new Setting<>(new IntegerSetting(), "userOrientation", null); }

	public Setting<Boolean> enabled() { return new Setting<>(new BooleanSetting(), "enabled", true); }
	public Setting<Boolean> allowUpsideDownScreen() { return new Setting<>(new BooleanSetting(), "allowUpsideDownScreen", false); }
	public Setting<ScreenOffBehaviour> screenOffBehaviour() { return new Setting<>(new EnumSetting<>(ScreenOffBehaviour.class), "screenOnBehaviour", ScreenOffBehaviour.DO_NOTHING); }
	public Setting<Boolean> showNotificationAtTop() { return new Setting<>(new BooleanSetting(), "showNotificationAtTop", false); }
	public Setting<Set<String>> forcedOrientationAppPackages() { return new Setting<>(new StringSetSetting(), "forcedOrientationAppPackages", new HashSet<String>(0)); }

	public class Setting<T> {

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

		public EventRegistrar<Void> onChange() {
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
				new EnvironmentErrorNotifier(context).handle("Couldn't write application settings.");
		}
	}

}
