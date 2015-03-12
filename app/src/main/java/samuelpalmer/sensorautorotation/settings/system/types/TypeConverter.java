package samuelpalmer.sensorautorotation.settings.system.types;

public interface TypeConverter<T> {
	T tryParse(String nonNullTextValue);

	String format(T nonNullValue);
}
