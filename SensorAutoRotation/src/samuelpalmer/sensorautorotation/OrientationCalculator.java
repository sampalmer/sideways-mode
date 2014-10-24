package samuelpalmer.sensorautorotation;


public class OrientationCalculator {
	
	private Orientation device;
	private Orientation screen;
	private boolean covered;
	
	public OrientationCalculator(Orientation screen, Orientation device, boolean covered) {
		this.screen = screen;
		this.device = device;
		this.covered = covered;
	}
	
	public void registerNewDeviceOrientation(Orientation newDevice) {
		Rotation change = newDevice.minus(device);
	
		if (change.isNinetyDegree())
			handleNinetyDegreeRotation(change);
		
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
