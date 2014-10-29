package samuelpalmer.sensorautorotation.systemconfiguration;

public class ConvertingTypedSystemConfig<T> implements TypedSystemConfig<T> {
	
	private UntypedSystemConfig untyped;
	private SystemConfigTypeConverter<T> converter;

	public ConvertingTypedSystemConfig(UntypedSystemConfig untyped, SystemConfigTypeConverter<T> converter) {
		this.untyped = untyped;
		this.converter = converter;
	}
	
	@Override
	public T getOrNull(String key) {
		String textValue = untyped.getOrNull(key);
		if (textValue == null)
			return null;
		
		return converter.convertOrNull(textValue);
	}

	@Override
	public void set(String key, T value) throws SettingWriteFailedException {
		String textValue = value == null ? null : converter.convert(value);
		untyped.set(key, textValue);
	}

}
