package samuelpalmer.sensorautorotation.processes.service;

import samuelpalmer.sensorautorotation.settings.CachedSetting;
import samuelpalmer.sensorautorotation.settings.application.Settings.Transaction;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ApplicationState {

	private final ServiceSettings serviceSettings;
	public final CachedSetting<Boolean> autoRotate;
	public final CachedSetting<Orientation> user;

	public ApplicationState(SystemSettings systemSettings) {
		serviceSettings = new ServiceSettings();
		autoRotate = new CachedSetting<>(serviceSettings.autoRotate());
		user = new CachedSetting<>(serviceSettings.userOrientation());

		if (!serviceSettings.autoRotate().hasValue()) {
			boolean accelerometerRotation = systemSettings.accelerometerRotation.get();
			autoRotate.set(accelerometerRotation);
		}

		if (!serviceSettings.userOrientation().hasValue())
			user.set(Orientation.NATURAL);
	}

	public void set(boolean autoRotate, Orientation user) {
		Transaction transaction = serviceSettings.new Transaction();
		serviceSettings.autoRotate().enqueue(autoRotate, transaction);
		serviceSettings.userOrientation().enqueue(user, transaction);
		transaction.commit();

		this.autoRotate.updateCache(autoRotate);
		this.user.updateCache(user);
	}

	public void clear() {
		Transaction transaction = serviceSettings.new Transaction();
		serviceSettings.autoRotate().remove(transaction);
		serviceSettings.userOrientation().remove(transaction);
		transaction.commit();

		this.autoRotate.updateCache(null);
		this.user.updateCache(null);
	}

}
