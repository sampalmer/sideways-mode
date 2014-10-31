package samuelpalmer.sensorautorotation.systemconfiguration;

import android.provider.Settings;
import samuelpalmer.sensorautorotation.Orientation;
import samuelpalmer.sensorautorotation.systemconfiguration.types.TypeConverters;

public class SystemSettings {
	
	public SystemSettings(SystemSettingSource config) {
		accelerometerRotation = new Setting<Boolean>(config, Settings.System.ACCELEROMETER_ROTATION, TypeConverters.bool);
		userRotation = new Setting<Orientation>(config, Settings.System.USER_ROTATION, TypeConverters.orientation);
	}
	
	public final Setting<Boolean> accelerometerRotation;
	public final Setting<Orientation> userRotation;
	
}
