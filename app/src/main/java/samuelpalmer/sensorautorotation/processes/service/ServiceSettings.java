package samuelpalmer.sensorautorotation.processes.service;

import android.content.Context;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.settings.application.BooleanSetting;
import samuelpalmer.common.settings.application.OrientationSetting;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.Process;

/**
 * Holds service state using standard non-process-safe preferences for performance.
 */
public class ServiceSettings extends Settings {

	public ServiceSettings() {
		super(Process.context().getSharedPreferences("service", Context.MODE_PRIVATE));
	}

	public Setting<Boolean> autoRotate() { return new Setting<>(new BooleanSetting(), "autoRotate", null); }
	public Setting<Orientation> userOrientation() { return new Setting<>(new OrientationSetting(), "userOrientation", null); }

}
