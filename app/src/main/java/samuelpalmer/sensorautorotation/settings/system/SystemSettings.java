package samuelpalmer.sensorautorotation.settings.system;

import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.settings.system.types.BooleanTypeConverter;
import samuelpalmer.sensorautorotation.settings.system.types.OrientationTypeConverter;

import static android.provider.Settings.System.ACCELEROMETER_ROTATION;
import static android.provider.Settings.System.USER_ROTATION;

public class SystemSettings {

	public final Setting<Boolean> accelerometerRotation = new Setting<>(ACCELEROMETER_ROTATION, new BooleanTypeConverter(), true);
	public final Setting<Orientation> userRotation = new Setting<>(USER_ROTATION, new OrientationTypeConverter(), Orientation.NATURAL);

}
