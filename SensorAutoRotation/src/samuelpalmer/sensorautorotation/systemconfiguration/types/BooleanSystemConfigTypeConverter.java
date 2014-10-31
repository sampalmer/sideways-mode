package samuelpalmer.sensorautorotation.systemconfiguration.types;

public class BooleanSystemConfigTypeConverter implements
		SystemConfigTypeConverter<Boolean> {
	
	private static final int FALSE = 0;
	private static final int TRUE = 1;
	
	private final SystemConfigTypeConverter<Integer> integerConverter;

	public BooleanSystemConfigTypeConverter() {
		this.integerConverter = new IntegerSystemConfigTypeConverter();
	}
	
	@Override
	public Boolean parse(String nonNullTextValue) {
		Integer integerValue = integerConverter.parse(nonNullTextValue);
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
	public String format(Boolean nonNullvalue) {
		Integer integerValue = nonNullvalue ? TRUE : FALSE;
		return integerConverter.format(integerValue);
	}

}
