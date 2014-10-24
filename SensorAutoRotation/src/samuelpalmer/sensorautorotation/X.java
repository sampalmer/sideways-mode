package samuelpalmer.sensorautorotation;

public class X {
	
	private final Screen orientationChanger;
	private OrientationCalculator orientationCalculator;
	
	private Orientation initialDeviceOrientation;
	private boolean hasInitialCoveredState;
	private boolean initialCoveredState;
	
	public X(Screen orientationChanger) {
		this.orientationChanger = orientationChanger;
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
			orientationChanger.set(screenOrientation);
		}
	}

	private Orientation initialScreenOrientation() {
		return orientationChanger.getOriginal();
	}
	
	private boolean canMakeCalculator() {
		return initialDeviceOrientation != null && hasInitialCoveredState;
	}
}
