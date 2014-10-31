package samuelpalmer.sensorautorotation.systemconfiguration;

import samuelpalmer.sensorautorotation.systemconfiguration.types.SystemConfigTypeConverter;

public class Setting<T> {
	
	private SystemConfig config;
	private String key;
	private SystemConfigTypeConverter<T> converter;

	public Setting(SystemConfig config, String key, SystemConfigTypeConverter<T> converter) {
		this.config = config;
		this.key = key;
		this.converter = converter;
	}
	
	public T getOrNull() {
		String value = config.getOrNull(key);
		if (value == null)
			return null;
		
		return converter.convertOrNull(value);
	}

	public void set(T value) throws SettingWriteFailedException {
		String textValue = converter.convert(value);
		config.set(key, textValue);
	}
	
}
