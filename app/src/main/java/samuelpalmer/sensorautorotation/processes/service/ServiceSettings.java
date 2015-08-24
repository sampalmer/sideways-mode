package samuelpalmer.sensorautorotation.processes.service;

import android.content.Context;

import samuelpalmer.sensorautorotation.Process;
import samuelpalmer.sensorautorotation.settings.application.BooleanSetting;
import samuelpalmer.sensorautorotation.settings.application.IntegerSetting;
import samuelpalmer.sensorautorotation.settings.application.Settings;

/**
 * Holds service state using standard non-process-safe preferences for performance.
 */
public class ServiceSettings extends Settings {

	public ServiceSettings() {
		super(Process.context().getSharedPreferences("service", Context.MODE_PRIVATE));
	}

	public Setting<Boolean> autoRotate() { return new Setting<>(new BooleanSetting(), "autoRotate", null); }
	public Setting<Integer> userOrientation() { return new Setting<>(new IntegerSetting(), "userOrientation", null); }

}
