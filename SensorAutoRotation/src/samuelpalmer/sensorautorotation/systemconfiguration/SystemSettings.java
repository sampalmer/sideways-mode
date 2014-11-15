package samuelpalmer.sensorautorotation.systemconfiguration;

import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.systemconfiguration.types.BooleanTypeConverter;
import samuelpalmer.sensorautorotation.systemconfiguration.types.OrientationTypeConverter;
import samuelpalmer.sensorautorotation.systemconfiguration.types.TypeConverter;

import static android.provider.Settings.System.*;

public class SystemSettings {
	
	private SystemSettingSource config;
	
	public SystemSettings(SystemSettingSource config) {
		this.config = config;
		
		accelerometerRotation = makeSetting(ACCELEROMETER_ROTATION, new BooleanTypeConverter());
		userRotation = makeSetting(USER_ROTATION, new OrientationTypeConverter());
	}
	
	private <T> Setting<T> makeSetting(String key, TypeConverter<T> type) {
		return new Setting<T>(config, key, type);
	}
	
	public final Setting<Boolean> accelerometerRotation;
	public final Setting<Orientation> userRotation;
	
}
