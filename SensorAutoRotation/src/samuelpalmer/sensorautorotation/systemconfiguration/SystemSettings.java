package samuelpalmer.sensorautorotation.systemconfiguration;

import android.provider.Settings;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.systemconfiguration.types.BooleanTypeConverter;
import samuelpalmer.sensorautorotation.systemconfiguration.types.OrientationTypeConverter;

public class SystemSettings {
	
	public SystemSettings(SystemSettingSource config) {
		accelerometerRotation = new Setting<Boolean>(config, Settings.System.ACCELEROMETER_ROTATION, new BooleanTypeConverter());
		userRotation = new Setting<Orientation>(config, Settings.System.USER_ROTATION, new OrientationTypeConverter());
	}
	
	public final Setting<Boolean> accelerometerRotation;
	public final Setting<Orientation> userRotation;
	
}
