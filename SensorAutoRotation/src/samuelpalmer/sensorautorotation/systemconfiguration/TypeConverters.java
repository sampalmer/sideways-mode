package samuelpalmer.sensorautorotation.systemconfiguration;

public class TypeConverters {
	
	public static final SystemConfigTypeConverter<Boolean> bool;
	public static final SystemConfigTypeConverter<Integer> integer;
	
	static {
		integer = new IntegerSystemConfigTypeConverter();
		bool = new BooleanSystemConfigTypeConverter(integer);
	}

}
