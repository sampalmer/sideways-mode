package samuelpalmer.sensorautorotation.systemconfiguration.types;

public interface TypeConverter<T> {
	T tryParse(String nonNullTextValue);
	String format(T nonNullvalue);
}
