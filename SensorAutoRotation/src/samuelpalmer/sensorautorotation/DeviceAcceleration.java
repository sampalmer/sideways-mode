package samuelpalmer.sensorautorotation;

/**
 * All units in m/s/s
 */
public class DeviceAcceleration {
	
	private Orientation orientation;

	public DeviceAcceleration(float x, float y) {
		final int threshold = 1;
		
		float xMagnitude = Math.abs(x);
		float yMagnitude = Math.abs(y);
		
		float differenceInMagnitudes = Math.abs(xMagnitude - yMagnitude);
		if (differenceInMagnitudes > threshold) {
			boolean isMoreVertical = yMagnitude > xMagnitude;
			
			int clockwiseRotationsFromNaturalOrientation;
			
			if (isMoreVertical && yMagnitude > threshold)
				if (y > 0)
					clockwiseRotationsFromNaturalOrientation = 0;
				else
					clockwiseRotationsFromNaturalOrientation = 2;
			else if (xMagnitude > threshold)
				if (x > 0)
					clockwiseRotationsFromNaturalOrientation = 1;
				else
					clockwiseRotationsFromNaturalOrientation = 3;
			else
				return;
			
			orientation = new Orientation(clockwiseRotationsFromNaturalOrientation);
		}
	}

	public boolean hasOrientation() {
		return orientation != null;
	}

	public Orientation orientation() {
		if (!hasOrientation())
			throw new RuntimeException("No orientation was determined");
		
		return orientation;
	}
	
}
