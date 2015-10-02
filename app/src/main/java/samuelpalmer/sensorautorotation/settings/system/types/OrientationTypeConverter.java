package samuelpalmer.sensorautorotation.settings.system.types;

import samuelpalmer.common.AndroidSurfaceRotation;
import samuelpalmer.common.Orientation;

public class OrientationTypeConverter implements TypeConverter<Orientation> {

	private final IntegerTypeConverter integer;

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
	public String format(Orientation nonNullValue) {
		int androidSurfaceRotation = new AndroidSurfaceRotation(nonNullValue).getValue();
		return integer.format(androidSurfaceRotation);
	}

}
