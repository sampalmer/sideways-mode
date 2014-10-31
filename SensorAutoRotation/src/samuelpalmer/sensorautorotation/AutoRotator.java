package samuelpalmer.sensorautorotation;

public class AutoRotator {
	
	private final Screen screen;
	private OrientationCalculator orientationCalculator;
	
	private Orientation initialDeviceOrientation;
	private Boolean initialCoveredState;
	
	public AutoRotator(Screen screen) {
		this.screen = screen;
	}
	
	public void setDeviceOrientation(Orientation value) {
		if (initialDeviceOrientation == null)
			initialDeviceOrientation = value;
		
		if (orientationCalculator != null)
			orientationCalculator.setDevice(value);
		
		update();
	}
	
	public void setProximitySensorCovered(boolean value) {
		if (initialCoveredState = null)
			initialCoveredState = value;
		
		if (orientationCalculator != null)
			orientationCalculator.setCovered(value);
		
		update();
	}
	
	private void update() {
		if (orientationCalculator == null && canMakeCalculator())
			orientationCalculator = new OrientationCalculator(initialScreenOrientation(), initialDeviceOrientation, initialCoveredState);
		
		if (orientationCalculator != null) {
			Orientation screenOrientation = orientationCalculator.screenOrientation();
			screen.set(screenOrientation);
		}
	}

	private Orientation initialScreenOrientation() {
		return screen.originalUserOrientation();
	}
	
	private boolean canMakeCalculator() {
		return initialDeviceOrientation != null && initialCoveredState != null;
	}
}
