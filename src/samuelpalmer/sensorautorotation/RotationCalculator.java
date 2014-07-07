package samuelpalmer.sensorautorotation;


public class RotationCalculator {
	
	private Orientation lastDevice;
	private Orientation screen;
	private boolean covered;
	
	public RotationCalculator(Orientation screen) {
		this.screen = screen;
	}
	
	public void registerNewDeviceOrientation(Orientation newDevice) {

		if (lastDevice == null)
			lastDevice = newDevice;
		
		boolean changed = lastDevice != newDevice;
		if (changed) {
			Rotation change = newDevice.minus(lastDevice);
		
			if (change.isNinetyDegree())
				handleNinetyDegreeRotation(change);
		}
		
		lastDevice = newDevice;
	}

	private void handleNinetyDegreeRotation(Rotation change) {
		if (screen.isPortrait()) {
			if (covered) {
				screen = screen.plus(change);
			}
		}
		else {
			if (!covered) {
				screen = screen.plus(change);
			}
		}
	}

	public void registerNewCoveredState(boolean covered) {
		this.covered = covered;
	}

	public Orientation screenOrientation() {
		return screen;
	}
	
}
