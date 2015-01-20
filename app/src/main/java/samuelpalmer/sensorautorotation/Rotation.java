package samuelpalmer.sensorautorotation;

public class Rotation {

	private final int clockwiseRotations;

	public Rotation(int clockwiseRotations) {
		this.clockwiseRotations = clockwiseRotations;
	}

	public int clockwiseCount() {
		return this.clockwiseRotations;
	}

}
