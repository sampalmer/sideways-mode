package samuelpalmer.sensorautorotation.systemconfiguration.types;

public class TypeConverters {
	
	public static final TypeConverter<Boolean> bool;
	public static final TypeConverter<Integer> integer;
	
	static {
		integer = new IntegerTypeConverter();
		bool = new BooleanTypeConverter();
	}

}
