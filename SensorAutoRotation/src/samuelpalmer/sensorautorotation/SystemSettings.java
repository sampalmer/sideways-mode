package samuelpalmer.sensorautorotation;

import android.provider.Settings;
import samuelpalmer.sensorautorotation.systemconfiguration.Setting;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemConfig;
import samuelpalmer.sensorautorotation.systemconfiguration.types.TypeConverters;

public class SystemSettings {
	
	public SystemSettings(SystemConfig config) {
		accelerometerRotation = new Setting<Boolean>(config, Settings.System.ACCELEROMETER_ROTATION, TypeConverters.bool);
		userRotation = new Setting<Integer>(config, Settings.System.USER_ROTATION, TypeConverters.integer);
	}
	
	public final Setting<Boolean> accelerometerRotation;
	public final Setting<Integer> userRotation;
	
}
