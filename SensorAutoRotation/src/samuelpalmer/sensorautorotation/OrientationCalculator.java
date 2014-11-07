package samuelpalmer.sensorautorotation;


public class OrientationCalculator {
	
	private Orientation device;
	private Boolean covered;
	private Screen screen;
	
	public OrientationCalculator(Screen screen) {
		this.screen = screen;
	}
	
	public void setDevice(Orientation newDevice) throws EnvironmentException {
		
		if (device != null && covered != null) {
			Rotation change = newDevice.minus(device);
			
			if (change.isNinetyDegree()) {
				Orientation screenOrientation = screen.get();
				
				if (screenOrientation.sideways() != covered) {
					Orientation proposedScreen = screenOrientation.plus(change);
					screen.set(proposedScreen);
				}
			}
		}
		
		device = newDevice;
	}

	public void setCovered(boolean covered) {
		this.covered = covered;
	}
	
}
