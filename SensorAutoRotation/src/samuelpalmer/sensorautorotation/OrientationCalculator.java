package samuelpalmer.sensorautorotation;


public class OrientationCalculator {
	
	private Orientation device;
	private Boolean covered;
	private Orientation deviceLock;
	private Screen screen;
	
	public OrientationCalculator(Screen screen) {
		this.screen = screen;
	}
	
	public void setDevice(Orientation newDevice) {
		
		Orientation screenOrientation = screen.get();
		
		if (device != null && covered != null) {
			Rotation change = newDevice.minus(device);
			Orientation proposedScreen = screenOrientation.plus(change);
			
			if (deviceLock != null && newDevice.equals(deviceLock))
				deviceLock = null;
			else
				if (change.isNinetyDegree())
					if (screenOrientation.isPortrait() == covered)
						if (!proposedScreen.equals(Orientation.REVERSE_PORTRAIT))
							screen.set(proposedScreen);
						else
							deviceLock = device;
		}
		
		device = newDevice;
	}

	public void setCovered(boolean covered) {
		this.covered = covered;
	}
	
}
