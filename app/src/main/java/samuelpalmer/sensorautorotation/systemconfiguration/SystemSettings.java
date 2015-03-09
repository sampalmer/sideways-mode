package samuelpalmer.sensorautorotation.systemconfiguration;

import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.systemconfiguration.types.BooleanTypeConverter;
import samuelpalmer.sensorautorotation.systemconfiguration.types.OrientationTypeConverter;
import samuelpalmer.sensorautorotation.systemconfiguration.types.TypeConverter;

import static android.provider.Settings.System.ACCELEROMETER_ROTATION;
import static android.provider.Settings.System.USER_ROTATION;

public class SystemSettings {

	private final SystemSettingSource config;

	public SystemSettings(SystemSettingSource config) {
		this.config = config;

		accelerometerRotation = makeSetting(ACCELEROMETER_ROTATION, new BooleanTypeConverter(), true);
		userRotation = makeSetting(USER_ROTATION, new OrientationTypeConverter(), Orientation.NATURAL);
	}

	private <T> Setting<T> makeSetting(String key, TypeConverter<T> type, T defaultValue) {
		return new Setting<>(config, key, type, defaultValue);
	}

	public final Setting<Boolean> accelerometerRotation;
	public final Setting<Orientation> userRotation;

}
