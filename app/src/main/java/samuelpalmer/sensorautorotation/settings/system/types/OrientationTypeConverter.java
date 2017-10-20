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
			return AndroidSurfaceRotation.toOrientationClockwise(androidSurfaceRotation);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	@Override
	public String format(Orientation nonNullValue) {
		int androidSurfaceRotation = AndroidSurfaceRotation.fromOrientationClockwise(nonNullValue);
		return integer.format(androidSurfaceRotation);
	}

}
