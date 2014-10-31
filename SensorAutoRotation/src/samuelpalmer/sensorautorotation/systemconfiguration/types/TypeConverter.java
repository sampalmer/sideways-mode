package samuelpalmer.sensorautorotation.systemconfiguration.types;

public interface TypeConverter<T> {
	T parse(String nonNullTextValue);
	String format(T nonNullvalue);
}
