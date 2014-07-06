package samuelpalmer.sensorautorotation;


public class RelativeOrientationCalculator implements OrientationCalculator {
	
	private Clock clock;
	
	private Orientation lastDeviceOrientation;
	private Orientation screenOrientation;
	private Rotation potentialRotationToLandscape;
	private boolean covered;
	
	public RelativeOrientationCalculator(Clock clock, int screenOrientation) {
		this.clock = clock;
		this.screenOrientation = Orientation.fromAndroid(screenOrientation);
	}
	
	@Override
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
		potentialRotationToLandscape = null;
		
		if (screenOrientation.isPortrait()) {
			if (covered) {
				screenOrientation = screenOrientation.rotatedClockwise(clockwiseRotations);
			}
//			else {
//				potentialRotationToLandscape = new Rotation();
//				potentialRotationToLandscape.clockwiseCount = clockwiseRotations;
//				potentialRotationToLandscape.when = clock.read();
//			}
		}
		else {
			if (!covered) {
				screenOrientation = screenOrientation.rotatedClockwise(clockwiseRotations);
			}
		}
	}

	@Override
	public void registerNewCoveredState(boolean covered) {
		boolean covering = !this.covered && covered;
		
		boolean justWentSideways = potentialRotationToLandscape != null && clock.read().getTime() - potentialRotationToLandscape.when.getTime() < 500;
		if (covering && justWentSideways) {
			screenOrientation = screenOrientation.rotatedClockwise(potentialRotationToLandscape.clockwiseCount);
			potentialRotationToLandscape = null;
		}
		
		this.covered = covered;
	}

	@Override
	public int getScreenOrientation() {
		return screenOrientation.getAndroidOrientation();
	}
	
}
