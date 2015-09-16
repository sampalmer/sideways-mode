package samuelpalmer.sensorautorotation.processes.service.upgrade;

import java.util.HashMap;
import java.util.Map;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.Orientation;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.BooleanSetting;
import samuelpalmer.sensorautorotation.settings.application.OrientationSetting;
import samuelpalmer.sensorautorotation.settings.application.Settings.Setting;
import samuelpalmer.sensorautorotation.settings.application.Settings.Transaction;
import samuelpalmer.sensorautorotation.settings.application.StringSetting;

class Migrations {
	static Map<Integer, Migration> map() {
		Map<Integer, Migration> migrations = new HashMap<>();

		migrations.put(18, new Migration(R.string.v18_upgrade));

		migrations.put(25, new Migration(null) {
			@Override
			public void upgradeData() {
				ApplicationSettings applicationSettings = ApplicationSettings.local();
				Setting<Boolean> oldAutoRotate = applicationSettings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Setting<Orientation> oldUserOrientation = applicationSettings.new Setting<Orientation>(new OrientationSetting(), "userOrientation", null);
				Setting<String> screenOffBehaviour = applicationSettings.new Setting<String>(new StringSetting(), "screenOnBehaviour", null);

				ServiceSettings serviceSettings = new ServiceSettings();
				Transaction copyTransaction = serviceSettings.new Transaction();
				copyIfPresent(oldAutoRotate, serviceSettings.autoRotate(), copyTransaction);
				copyIfPresent(oldUserOrientation, serviceSettings.userOrientation(), copyTransaction);
				copyTransaction.commit();

				Transaction deleteTransaction = applicationSettings.new Transaction();
				oldAutoRotate.remove(deleteTransaction);
				oldUserOrientation.remove(deleteTransaction);
				screenOffBehaviour.remove(deleteTransaction);
				deleteTransaction.commit();
			}

			private <T> void copyIfPresent(Setting<T> source, Setting<T> destination, Transaction transaction) {
				T value = source.tryGet();

				if (value != null)
					destination.set(value, transaction);
			}
		});

		return migrations;
	}
}
