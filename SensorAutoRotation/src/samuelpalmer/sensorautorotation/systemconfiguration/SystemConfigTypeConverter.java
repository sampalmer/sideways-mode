package samuelpalmer.sensorautorotation.systemconfiguration;

public interface SystemConfigTypeConverter<T> {
	T convertOrNull(String nonNullTextValue);
	String convert(T nonNullvalue);
}
