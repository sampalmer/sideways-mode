package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.Rotation;
import samuelpalmer.common.settings.application.Settings.Transaction;
import samuelpalmer.sensorautorotation.settings.CachedSetting;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ApplicationState {

	private final ServiceSettings serviceSettings;
	public final CachedSetting<Boolean> autoRotate;
	public final CachedSetting<Rotation> offset;

	public ApplicationState(SystemSettings systemSettings) {
		serviceSettings = new ServiceSettings();
		autoRotate = new CachedSetting<>(serviceSettings.autoRotate());
		offset = new CachedSetting<>(serviceSettings.screenOrientationOffset());

		if (!serviceSettings.autoRotate().hasValue()) {
			boolean accelerometerRotation = systemSettings.accelerometerRotation.get();
			autoRotate.set(accelerometerRotation);
		}

		if (!serviceSettings.screenOrientationOffset().hasValue())
			offset.set(new Rotation(0));
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
