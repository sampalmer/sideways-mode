package samuelpalmer.sensorautorotation.systemconfiguration.types;

public class BooleanTypeConverter implements
		TypeConverter<Boolean> {

	private static final int FALSE = 0;
	private static final int TRUE = 1;

	private final TypeConverter<Integer> integerConverter;

	public BooleanTypeConverter() {
		this.integerConverter = new IntegerTypeConverter();
	}

	@Override
	public Boolean tryParse(String nonNullTextValue) {
		Integer integerValue = integerConverter.tryParse(nonNullTextValue);
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
