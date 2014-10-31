package samuelpalmer.sensorautorotation.systemconfiguration.types;

public interface SystemConfigTypeConverter<T> {
	T parse(String nonNullTextValue);
	String format(T nonNullvalue);
}
