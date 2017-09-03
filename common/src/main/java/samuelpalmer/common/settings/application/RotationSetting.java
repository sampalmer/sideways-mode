package samuelpalmer.common.settings.application;

import samuelpalmer.common.Rotation;

public class RotationSetting extends WrapperSetting<Rotation, Integer> {
	public RotationSetting() {
		super(new IntegerSetting());
	}

	@Override
	protected Integer convertToUnderlying(Rotation rotation) {
		return rotation.clockwiseCount();
	}

	@Override
	protected Rotation convertFromUnderlying(Integer integer) {
		return new Rotation(integer);
	}
}
