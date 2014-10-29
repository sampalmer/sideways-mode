package samuelpalmer.sensorautorotation.systemconfiguration;

public class Setting<T> {
	
	private UntypedSystemConfig untyped;
	private String key;
	private SystemConfigTypeConverter<T> converter;

	public Setting(UntypedSystemConfig untyped, String key, SystemConfigTypeConverter<T> converter) {
		this.untyped = untyped;
		this.key = key;
		this.converter = converter;
	}
	
	public T getOrNull() {
		String value = untyped.getOrNull(key);
		if (value == null)
			return null;
		
		return converter.convertOrNull(value);
	}

	public void set(T value) throws SettingWriteFailedException {
		String textValue = converter.convert(value);
		untyped.set(key, textValue);
	}
	
}
