package samuelpalmer.sensorautorotation.systemconfiguration;

public class SystemConfig {
	
	private UntypedSystemConfig untyped;

	public SystemConfig(UntypedSystemConfig untyped) {
		this.untyped = untyped;
	}
	
	public <T> T getOrNull(String key, SystemConfigTypeConverter<T> converter) {
		String value = untyped.getOrNull(key);
		if (value == null)
			return null;
		
		return converter.convertOrNull(value);
	}

	public <T> void set(String key, T value, SystemConfigTypeConverter<T> converter) throws SettingWriteFailedException {
		String textValue = converter.convert(value);
		untyped.set(key, textValue);
	}
	
}
