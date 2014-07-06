package samuelpalmer.sensorautorotation;


public class OrientationCalculator {
	
	private Orientation lastDeviceOrientation;
	private Orientation screenOrientation;
	private boolean covered;
	
	public OrientationCalculator(int screenOrientation) {
		this.screenOrientation = Orientation.fromAndroid(screenOrientation);
	}
	
	public void registerNewDeviceOrientation(int deviceOrientation) {
		Orientation newDeviceOrientation = Orientation.fromAndroid(deviceOrientation);

		if (lastDeviceOrientation == null)
			lastDeviceOrientation = newDeviceOrientation;
		
		boolean changed = lastDeviceOrientation != newDeviceOrientation;
		if (changed) {
			int clockwiseRotations = newDeviceOrientation.clockwiseRotationsFrom(lastDeviceOrientation);
		
			boolean isNinetyDegree = clockwiseRotations % 2 != 0;
			if (isNinetyDegree)
				handleNinetyDegreeRotation(clockwiseRotations);
		}
		
		lastDeviceOrientation = newDeviceOrientation;
	}

	private void handleNinetyDegreeRotation(int clockwiseRotations) {
		if (screenOrientation.isPortrait()) {
			if (covered) {
				screenOrientation = screenOrientation.rotatedClockwise(clockwiseRotations);
			}
		}
		else {
			if (!covered) {
				screenOrientation = screenOrientation.rotatedClockwise(clockwiseRotations);
			}
		}
	}

	public void registerNewCoveredState(boolean covered) {
		this.covered = covered;
	}

	public int getScreenOrientation() {
		return screenOrientation.getAndroidOrientation();
	}
	
}
