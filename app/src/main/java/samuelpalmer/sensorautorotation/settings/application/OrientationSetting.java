package samuelpalmer.sensorautorotation.settings.application;

import samuelpalmer.sensorautorotation.processes.service.Orientation;

public class OrientationSetting extends WrapperSetting<Orientation, Integer> {

	public OrientationSetting() {
		super(new IntegerSetting());
	}

	@Override
	protected Integer convertToUnderlying(Orientation orientation) {
		return orientation.getClockwiseRotationsFromNaturalOrientation();
	}

	@Override
	protected Orientation convertFromUnderlying(Integer integer) {
		return new Orientation(integer);
	}

}
