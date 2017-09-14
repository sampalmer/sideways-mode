package samuelpalmer.sensorautorotation.processes.service;

import android.support.annotation.Nullable;

import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.settings.CachedSetting;

public class ApplicationState {

	private final ServiceSettings serviceSettings;
	public final CachedSetting<UserSide> userSide;

	public ApplicationState(@Nullable UserSide requestedUserSide) {
		serviceSettings = new ServiceSettings();
		userSide = new CachedSetting<>(serviceSettings.userSide());
		
		if (requestedUserSide != null)
			userSide.set(requestedUserSide);
		else if (!serviceSettings.userSide().hasValue())
			throw new RuntimeException("Don't know which side the user is on, and the requested user side is null.");
	}
	
	public void clear() {
		serviceSettings.userSide().remove();
		this.userSide.updateCache(null);
	}

}
