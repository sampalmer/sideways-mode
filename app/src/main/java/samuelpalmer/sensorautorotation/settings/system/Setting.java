package samuelpalmer.sensorautorotation.settings.system;

import android.content.ContentResolver;
import android.database.ContentObserver;

import samuelpalmer.sensorautorotation.ThisApplication;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.eventhandling.EventRegistrar;
import samuelpalmer.sensorautorotation.eventhandling.UpdatingEventRegistrar;
import samuelpalmer.sensorautorotation.settings.system.types.TypeConverter;

import static android.provider.Settings.System.*;

public class Setting<T> {

	private final ContentResolver contentResolver;
	private final String key;
	private final TypeConverter<T> converter;
	private final T defaultValue;

	public Setting(String key, TypeConverter<T> converter, T defaultValue) {
		this.contentResolver = ThisApplication.context().getContentResolver();
		this.key = key;
		this.converter = converter;
		this.defaultValue = defaultValue;
	}

	private final ChangeRegistrar onExternalChange = new ChangeRegistrar();
	public final EventRegistrar onExternalChange() { return onExternalChange; }

	private final ChangeRegistrar onChange = new ChangeRegistrar();
	public final EventRegistrar onChange() { return onChange; }

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
		onExternalChange.setEnabled(false);

		boolean writeErrors;
		try {
			writeErrors = !putString(contentResolver, key, value);
		}
		finally {
			onExternalChange.setEnabled(true);
		}

		if (writeErrors)
			throw new SettingWriteFailedException(key, value);
	}

	//Mostly copied from ApplicationSettings.Setting
	private class ChangeRegistrar extends UpdatingEventRegistrar {

		private boolean registered;
		private boolean enabled = true;

		private final ContentObserver listener = new ContentObserver(null) {
			@Override
			public void onChange(boolean selfChange) {
				super.onChange(selfChange);
				report();
			}
		};

		@Override
		public void subscribe(EventHandler subscriber) {
			super.subscribe(subscriber);
			updateRegistration();
		}

		@Override
		public void unsubscribe(EventHandler subscriber) {
			super.unsubscribe(subscriber);
			updateRegistration();
		}

		public void setEnabled(boolean newEnabled) {
			enabled = newEnabled;
			updateRegistration();
		}

		private boolean shouldBeRegistered() {
			return !noSubscriptions() && enabled;
		}

		private void updateRegistration() {
			if (shouldBeRegistered() != registered) {
				if (shouldBeRegistered())
					contentResolver.registerContentObserver(getUriFor(key), false, listener);
				else
					contentResolver.unregisterContentObserver(listener);

				registered = shouldBeRegistered();
			}
		}

	}
}
