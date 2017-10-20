package samuelpalmer.sensorautorotation.settings.application;

public class EnumSetting<T extends Enum<T>> extends WrapperSetting<T, String> {
	private final Class<T> clazz;

	public EnumSetting(Class<T> clazz) {
		super(new StringSetting());
		this.clazz = clazz;
	}

	@Override
	protected String convertToUnderlying(T value) {
		if (value == null)
			return null;
		else
			return value.name();
	}

	@Override
	protected T convertFromUnderlying(String underlying) {
		if (underlying == null)
			return null;
		else
			return Enum.valueOf(clazz, underlying);
	}
}
