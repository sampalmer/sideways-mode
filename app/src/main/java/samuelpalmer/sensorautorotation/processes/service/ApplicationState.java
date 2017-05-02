package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.Rotation;
import samuelpalmer.common.settings.application.Settings.Transaction;
import samuelpalmer.sensorautorotation.settings.CachedSetting;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ApplicationState {

	private final ServiceSettings serviceSettings;
	public final CachedSetting<Boolean> autoRotate;

	// Storing the offset rather than the actual user orientation since this way works
	// perfectly across app crashes and restarts.
	public final CachedSetting<Rotation> offset;

	public ApplicationState(SystemSettings systemSettings) {
		serviceSettings = new ServiceSettings();
		autoRotate = new CachedSetting<>(serviceSettings.autoRotate());
		offset = new CachedSetting<>(serviceSettings.screenOrientationOffset());

		if (!serviceSettings.autoRotate().hasValue()) { //TODO: Replace with !autoRotate.hasValue()?
			boolean accelerometerRotation = systemSettings.accelerometerRotation.get();
			autoRotate.set(accelerometerRotation);
		}

		if (!serviceSettings.screenOrientationOffset().hasValue()) //TODO: Replace with !offset.hasValue()?
			offset.set(autoRotate.get() ? new Rotation(0) : systemSettings.userRotation.get().minus(Orientation.NATURAL));
	}

	public void set(boolean autoRotate, Rotation offset) {
		Transaction transaction = serviceSettings.new Transaction();
		serviceSettings.autoRotate().enqueue(autoRotate, transaction);
		serviceSettings.screenOrientationOffset().enqueue(offset, transaction);
		transaction.commit();

		this.autoRotate.updateCache(autoRotate);
		this.offset.updateCache(offset);
	}

	public void clear() {
		Transaction transaction = serviceSettings.new Transaction();
		serviceSettings.autoRotate().remove(transaction);
		serviceSettings.screenOrientationOffset().remove(transaction);
		transaction.commit();

		this.autoRotate.updateCache(null);
		this.offset.updateCache(null);
	}

}
