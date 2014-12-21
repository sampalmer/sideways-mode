package samuelpalmer.sensorautorotation;

public class ProximitySensorCoveredState {
	
	private final boolean covered;

	public ProximitySensorCoveredState(boolean covered) {
		this.covered = covered;
	}

	public boolean isCovered() {
		return covered;
	}
	
}
