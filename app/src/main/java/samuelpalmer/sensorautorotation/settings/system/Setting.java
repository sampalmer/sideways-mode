package samuelpalmer.sensorautorotation.settings.system;

import android.content.ContentResolver;

import samuelpalmer.sensorautorotation.settings.system.types.TypeConverter;

import static android.provider.Settings.System.getString;
import static android.provider.Settings.System.putString;

public class Setting<T> {

	private final ContentResolver contentResolver;
	private final String key;
	private final TypeConverter<T> converter;
	private final T defaultValue;

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
