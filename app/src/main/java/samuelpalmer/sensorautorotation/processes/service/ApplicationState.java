package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.settings.application.Settings.Transaction;
import samuelpalmer.sensorautorotation.settings.CachedSetting;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ApplicationState {

	private final ServiceSettings serviceSettings;
	public final CachedSetting<Boolean> autoRotate;
	public final CachedSetting<Orientation> offset; //TODO: Find a good name for this

	public ApplicationState(SystemSettings systemSettings) {
		serviceSettings = new ServiceSettings();
		autoRotate = new CachedSetting<>(serviceSettings.autoRotate());
		offset = new CachedSetting<>(serviceSettings.userOrientation());

		if (!serviceSettings.autoRotate().hasValue()) {
			boolean accelerometerRotation = systemSettings.accelerometerRotation.get();
			autoRotate.set(accelerometerRotation);
		}

		if (!serviceSettings.userOrientation().hasValue())
			offset.set(Orientation.NATURAL);
	}

	public void set(boolean autoRotate, Orientation offset) {
		Transaction transaction = serviceSettings.new Transaction();
		serviceSettings.autoRotate().enqueue(autoRotate, transaction);
		serviceSettings.userOrientation().enqueue(offset, transaction);
		transaction.commit();

		this.autoRotate.updateCache(autoRotate);
		this.offset.updateCache(offset);
	}

	public void clear() {
		Transaction transaction = serviceSettings.new Transaction();
		serviceSettings.autoRotate().remove(transaction);
		serviceSettings.userOrientation().remove(transaction);
		transaction.commit();

		this.autoRotate.updateCache(null);
		this.offset.updateCache(null);
	}

}
