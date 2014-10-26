package samuelpalmer.sensorautorotation;


public class OrientationCalculator {
	
	private Orientation device;
	private Orientation screen;
	private boolean covered;
	private Orientation deviceLock;
	
	public OrientationCalculator(Orientation screen, Orientation device, boolean covered) {
		this.screen = screen;
		this.device = device;
		this.covered = covered;
	}
	
	public void registerNewDeviceOrientation(Orientation newDevice) {
		Rotation change = newDevice.minus(device);
		Orientation proposedScreen = screen.plus(change);
		
		if (deviceLock != null && newDevice.equals(deviceLock))
			deviceLock = null;
		else
			if (change.isNinetyDegree())
				if (screen.isPortrait() == covered)
					if (!proposedScreen.equals(Orientation.REVERSE_PORTRAIT))
						screen = proposedScreen;
					else
						deviceLock = device;
		
		device = newDevice;
	}

	public void registerNewCoveredState(boolean covered) {
		this.covered = covered;
	}

	public Orientation screenOrientation() {
		return screen;
	}
	
}
