package samuelpalmer.sensorautorotation.settings.system;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import samuelpalmer.sensorautorotation.settings.system.types.BooleanTypeConverter;
import samuelpalmer.sensorautorotation.settings.system.types.OrientationTypeConverter;
import samuelpalmer.sensorautorotation.values.Orientation;

import static android.provider.Settings.System.ACCELEROMETER_ROTATION;
import static android.provider.Settings.System.USER_ROTATION;

public class SystemSettings {

	public final Setting<Boolean> accelerometerRotation = new Setting<>(ACCELEROMETER_ROTATION, new BooleanTypeConverter(), true);
	public final Setting<Orientation> userRotation = new Setting<>(USER_ROTATION, new OrientationTypeConverter(), Orientation.NATURAL);
	
	public static boolean canWrite(Context context) {
		return Build.VERSION.SDK_INT < 23 || Settings.System.canWrite(context);
	}
	
}
