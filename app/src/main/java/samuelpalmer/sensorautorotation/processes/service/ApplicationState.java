package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.settings.CachedSetting;
import samuelpalmer.sensorautorotation.settings.application.ServiceSettings;

public class ApplicationState {
	
	public final CachedSetting<UserSide> userSide;

	public ApplicationState() {
		if (!ServiceSettings.instance().persistentState().hasValue())
			throw new RuntimeException("Don't know which side the user is on, and the requested user side is null.");
		
		userSide = new CachedSetting<>(ServiceSettings.instance().persistentState());
	}
	
	public void clear() {
		ServiceSettings.instance().persistentState().remove();
		this.userSide.updateCache(null);
	}

}
