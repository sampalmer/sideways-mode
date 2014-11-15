package samuelpalmer.sensorautorotation.systemconfiguration.types;

import samuelpalmer.sensorautorotation.Orientation;

public class OrientationTypeConverter implements TypeConverter<Orientation> {
	
	private IntegerTypeConverter integer;

	public OrientationTypeConverter() {
		integer = new IntegerTypeConverter();
	}
	
	@Override
	public Orientation tryParse(String nonNullTextValue) {
		Integer androidSurfaceRotation = integer.tryParse(nonNullTextValue);
		if (androidSurfaceRotation == null)
			return null;
		
		try {
			return Orientation.fromAndroidSurfaceRotation(androidSurfaceRotation);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public String format(Orientation nonNullvalue) {
		int androidSurfaceRotation = nonNullvalue.androidSurfaceRotation();
		return integer.format(androidSurfaceRotation);
	}

}
