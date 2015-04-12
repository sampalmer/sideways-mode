package samuelpalmer.sensorautorotation.settings.system;

import android.content.ContentResolver;
import android.database.ContentObserver;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.settings.system.types.TypeConverter;

import static android.provider.Settings.System.getString;
import static android.provider.Settings.System.getUriFor;
import static android.provider.Settings.System.putString;

public class Setting<T> {

	private final ContentResolver contentResolver;
	private final String key;
	private final TypeConverter<T> converter;
	private final T defaultValue;

	//Mostly copied from ApplicationSettings.Setting
	public final EventRegistrar<Void> onExternalChange = new UpdatingEventRegistrar<Void>() {

		private final ContentObserver listener = new ContentObserver(null) {

			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				report(null);
			}

			@Override
			public boolean deliverSelfNotifications() {
				return false;
			}
		};

		@Override
		public void subscribe(EventHandler<Void> subscriber) {
			if (noSubscriptions())
				contentResolver.registerContentObserver(getUriFor(key), false, listener);
			super.subscribe(subscriber);
		}

		@Override
		public void unsubscribe(EventHandler<Void> subscriber) {
			super.unsubscribe(subscriber);
			if (noSubscriptions())
				contentResolver.unregisterContentObserver(listener);
		}

	};

	public Setting(ContentResolver contentResolver, String key, TypeConverter<T> converter, T defaultValue) {
		this.contentResolver = contentResolver;
		this.key = key;
		this.converter = converter;
		this.defaultValue = defaultValue;
	}

	public T get() {
		String value = getOrNull(key);
		if (value == null)
			return defaultValue;

		return converter.tryParse(value);
	}

	public void set(T value) throws SettingWriteFailedException {
		String textValue = converter.format(value);
		set(key, textValue);
	}

	private String getOrNull(String key) {
		return getString(contentResolver, key);
	}

	private void set(String key, String value) throws SettingWriteFailedException {
		boolean writeErrors = !putString(contentResolver, key, value);
		if (writeErrors)
			throw new SettingWriteFailedException(key, value);
	}

}
