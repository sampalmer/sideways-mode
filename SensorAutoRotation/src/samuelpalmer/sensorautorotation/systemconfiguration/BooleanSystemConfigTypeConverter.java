package samuelpalmer.sensorautorotation.systemconfiguration;

public class BooleanSystemConfigTypeConverter implements
		SystemConfigTypeConverter<Boolean> {
	
	private static final int FALSE = 0;
	private static final int TRUE = 1;
	
	private SystemConfigTypeConverter<Integer> integerConverter;

	public BooleanSystemConfigTypeConverter(SystemConfigTypeConverter<Integer> integerConverter) {
		this.integerConverter = integerConverter;
	}
	
	@Override
	public Boolean convertOrNull(String nonNullTextValue) {
		Integer integerValue = integerConverter.convertOrNull(nonNullTextValue);
		if (integerValue == null)
			return null;
		
		switch (integerValue) {
			case FALSE:
				return false;
			case TRUE:
				return true;
			default:
				return null;
		}
	}

	@Override
	public String convert(Boolean nonNullvalue) {
		Integer integerValue = nonNullvalue ? TRUE : FALSE;
		return integerConverter.convert(integerValue);
	}

}
