package samuelpalmer.sensorautorotation.systemconfiguration.types;

public class IntegerSystemConfigTypeConverter implements SystemConfigTypeConverter<Integer> {

	@Override
	public Integer convertOrNull(String textValue) {
		try {
			return Integer.parseInt(textValue);
        } catch (NumberFormatException e) {
            return null;
        }
	}

	@Override
	public String convert(Integer value) {
		return Integer.toString(value);
	}

}
