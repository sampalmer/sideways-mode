package samuelpalmer.sensorautorotation.systemconfiguration.types;

import samuelpalmer.sensorautorotation.AndroidSurfaceRotation;
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
			return new AndroidSurfaceRotation(androidSurfaceRotation).orientation();
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public String format(Orientation nonNullvalue) {
		int androidSurfaceRotation = new AndroidSurfaceRotation(nonNullvalue).getValue();
		return integer.format(androidSurfaceRotation);
	}

}
