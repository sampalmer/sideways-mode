package samuelpalmer.sensorautorotation.processes.service;

import android.content.Context;

import samuelpalmer.common.Rotation;
import samuelpalmer.common.settings.application.BooleanSetting;
import samuelpalmer.common.settings.application.RotationSetting;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.CurrentProcess;

/**
 * Holds service state using standard non-process-safe preferences for performance.
 */
public class ServiceSettings extends Settings {

	public ServiceSettings() {
		super(CurrentProcess.context().getSharedPreferences("service", Context.MODE_PRIVATE));
	}

	public Setting<Boolean> autoRotate() { return new Setting<>(new BooleanSetting(), "autoRotate", null); }
	public Setting<Rotation> screenOrientationOffset() { return new Setting<>(new RotationSetting(), "screenOrientationOffset", null); }

}
