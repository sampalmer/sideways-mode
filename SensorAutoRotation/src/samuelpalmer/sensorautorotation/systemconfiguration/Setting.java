package samuelpalmer.sensorautorotation.systemconfiguration;

import samuelpalmer.sensorautorotation.systemconfiguration.types.TypeConverter;

public class Setting<T> {
	
	private SystemSettingSource config;
	private String key;
	private TypeConverter<T> converter;

	public Setting(SystemSettingSource config, String key, TypeConverter<T> converter) {
		this.config = config;
		this.key = key;
		this.converter = converter;
	}
	
	public T getOrNull() {
		String value = config.getOrNull(key);
		if (value == null)
			return null;
		
		return converter.tryParse(value);
	}

	public void set(T value) throws SettingWriteFailedException {
		String textValue = converter.format(value);
		config.set(key, textValue);
	}
	
}
