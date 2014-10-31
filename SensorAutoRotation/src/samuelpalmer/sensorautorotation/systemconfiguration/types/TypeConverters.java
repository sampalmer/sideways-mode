package samuelpalmer.sensorautorotation.systemconfiguration.types;

import samuelpalmer.sensorautorotation.Orientation;

public class TypeConverters {
	
	public static final TypeConverter<Boolean> bool;
	public static final TypeConverter<Integer> integer;
	public static final TypeConverter<Orientation> orientation;
	
	static {
		integer = new IntegerTypeConverter();
		bool = new BooleanTypeConverter();
		orientation = new OrientationTypeConverter();
	}

}
