package samuelpalmer.sensorautorotation.systemconfiguration;

import samuelpalmer.sensorautorotation.systemconfiguration.types.TypeConverter;

public class Setting<T> {

	private final SystemSettingSource config;
	private final String key;
	private final TypeConverter<T> converter;
	private final T defaultValue;

	public Setting(SystemSettingSource config, String key, TypeConverter<T> converter, T defaultValue) {
		this.config = config;
		this.key = key;
		this.converter = converter;
		this.defaultValue = defaultValue;
	}

	public T get() {
		String value = config.getOrNull(key);
		if (value == null)
			return defaultValue;

		return converter.tryParse(value);
	}

	public void set(T value) throws SettingWriteFailedException {
		String textValue = converter.format(value);
		config.set(key, textValue);
	}

}
