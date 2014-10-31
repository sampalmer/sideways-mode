package samuelpalmer.sensorautorotation.systemconfiguration.types;

public interface SystemConfigTypeConverter<T> {
	T convertOrNull(String nonNullTextValue);
	String convert(T nonNullvalue);
}
