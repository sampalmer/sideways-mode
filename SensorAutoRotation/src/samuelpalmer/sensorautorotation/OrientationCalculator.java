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
		
		if (device != null && covered != null) {
			if (deviceLock != null && newDevice.equals(deviceLock))
				deviceLock = null;
			else {
				Rotation change = newDevice.minus(device);
				
				if (change.isNinetyDegree()) {
					Orientation screenOrientation = screen.get();
					
					if (screenOrientation.sideways() != covered) {
						Orientation proposedScreen = screenOrientation.plus(change);
						
						if (!proposedScreen.equals(Orientation.UPSIDE_DOWN))
							screen.set(proposedScreen);
						else
							deviceLock = device;
					}
				}
			}
		}
		
		device = newDevice;
	}

	public void setCovered(boolean covered) {
		this.covered = covered;
	}
	
}
