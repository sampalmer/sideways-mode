package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.Rotation;
import samuelpalmer.sensorautorotation.settings.CachedSetting;

public class ApplicationState {

	private final ServiceSettings serviceSettings;
	
	// Storing the offset rather than the actual user orientation since this way works
	// perfectly across app crashes and restarts.
	public final CachedSetting<Rotation> offset;

	public ApplicationState() {
		serviceSettings = new ServiceSettings();
		offset = new CachedSetting<>(serviceSettings.screenOrientationOffset());
		
		if (!serviceSettings.screenOrientationOffset().hasValue()) //TODO: Replace with !offset.hasValue()?
			offset.set(new Rotation(0));
	}
	
	public void clear() {
		serviceSettings.screenOrientationOffset().remove();
		this.offset.updateCache(null);
	}

}
