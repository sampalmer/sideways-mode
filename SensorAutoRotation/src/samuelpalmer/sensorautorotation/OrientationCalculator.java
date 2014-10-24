package samuelpalmer.sensorautorotation;


public class OrientationCalculator {
	
	private Orientation device;
	private Orientation screen;
	private boolean covered;
	
	public OrientationCalculator(Orientation screen) {
		this.screen = screen;
	}
	
	public void registerNewDeviceOrientation(Orientation newDevice) {
		if (device == null)
			device = newDevice;
		
		boolean changed = device != newDevice;
		if (changed) {
			Rotation change = newDevice.minus(device);
		
			if (change.isNinetyDegree())
				handleNinetyDegreeRotation(change);
		}
		
		device = newDevice;
	}

	private void handleNinetyDegreeRotation(Rotation change) {
		if (screen.isPortrait() == covered)
			screen = screen.plus(change);
	}

	public void registerNewCoveredState(boolean covered) {
		this.covered = covered;
	}

	public Orientation screenOrientation() {
		return screen;
	}
	
}
