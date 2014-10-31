package samuelpalmer.sensorautorotation.systemconfiguration.types;

public class IntegerTypeConverter implements TypeConverter<Integer> {

	@Override
	public Integer tryParse(String textValue) {
		try {
			return Integer.parseInt(textValue);
        } catch (NumberFormatException e) {
            return null;
        }
	}

	@Override
	public String format(Integer value) {
		return Integer.toString(value);
	}

}
