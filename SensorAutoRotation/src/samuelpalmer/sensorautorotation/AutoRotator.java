package samuelpalmer.sensorautorotation;

public class AutoRotator {
	
	private final Screen screen;
	private OrientationCalculator orientationCalculator;
	
	private Orientation initialDeviceOrientation;
	private boolean hasInitialCoveredState;
	private boolean initialCoveredState;
	
	public AutoRotator(Screen screen) {
		this.screen = screen;
	}
	
	public void setDeviceOrientation(Orientation value) {
		if (initialDeviceOrientation == null)
			initialDeviceOrientation = value;
		
		if (orientationCalculator != null)
			orientationCalculator.registerNewDeviceOrientation(value);
		
		update();
	}
	
	public void setProximitySensorCovered(boolean value) {
		if (!hasInitialCoveredState) {
			initialCoveredState = value;
			hasInitialCoveredState = true;
		}
		
		if (orientationCalculator != null)
			orientationCalculator.registerNewCoveredState(value);
		
		update();
	}
	
	private void update() {
		if (orientationCalculator == null && canMakeCalculator())
			orientationCalculator = new OrientationCalculator(initialScreenOrientation(), initialDeviceOrientation, initialCoveredState);
		
		if (orientationCalculator != null) {
			Orientation screenOrientation = orientationCalculator.screenOrientation();
			try {
				screen.set(screenOrientation);
			} catch (EnvironmentException e) {
			}
		}
	}

	private Orientation initialScreenOrientation() {
		return screen.originalUserOrientation();
	}
	
	private boolean canMakeCalculator() {
		return initialDeviceOrientation != null && hasInitialCoveredState;
	}
}
