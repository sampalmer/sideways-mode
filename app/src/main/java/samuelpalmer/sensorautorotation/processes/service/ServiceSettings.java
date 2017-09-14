package samuelpalmer.sensorautorotation.processes.service;

import android.content.Context;

import samuelpalmer.common.settings.application.EnumSetting;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.UserSide;

/**
 * Holds service state using standard non-process-safe preferences for performance.
 */
public class ServiceSettings extends Settings {

	public ServiceSettings() {
		super(CurrentProcess.context().getSharedPreferences("service", Context.MODE_PRIVATE));
	}
	
	public Setting<UserSide> userSide() { return new Setting<>(new EnumSetting<>(UserSide.class), "userSide", null); }

}
