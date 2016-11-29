package samuelpalmer.sensorautorotation.processes.service.upgrade;

import java.util.HashMap;
import java.util.Map;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.settings.application.BooleanSetting;
import samuelpalmer.common.settings.application.OrientationSetting;
import samuelpalmer.common.settings.application.Settings.Setting;
import samuelpalmer.common.settings.application.Settings.Transaction;
import samuelpalmer.common.settings.application.StringSetting;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

class Migrations {
	static Map<Integer, Migration> map() {
		Map<Integer, Migration> migrations = new HashMap<>();

		migrations.put(18, new Migration(R.string.upgrade_v18));

		migrations.put(25, new Migration(null) {
			@Override
			public void upgradeData() {
				ApplicationSettings applicationSettings = ApplicationSettings.local();
				Setting<Boolean> oldAutoRotate = applicationSettings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Setting<Orientation> oldUserOrientation = applicationSettings.new Setting<Orientation>(new OrientationSetting(), "userOrientation", null);
				Setting<String> screenOffBehaviour = applicationSettings.new Setting<String>(new StringSetting(), "screenOnBehaviour", null);

				ServiceSettings serviceSettings = new ServiceSettings();
				Setting<Orientation> newUserOrientation = serviceSettings.new Setting<Orientation>(new OrientationSetting(), "userOrientation", null);

				Transaction copyTransaction = serviceSettings.new Transaction();
				copyIfPresent(oldAutoRotate, serviceSettings.autoRotate(), copyTransaction);
				copyIfPresent(oldUserOrientation, newUserOrientation, copyTransaction);
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
					destination.enqueue(value, transaction);
			}
		});

		migrations.put(43, new Migration(null) {
			@Override
			public void upgradeData() {
				super.upgradeData();

				ServiceSettings serviceSettings = new ServiceSettings();
				Setting<Orientation> oldUserOrientation = serviceSettings.new Setting<Orientation>(new OrientationSetting(), "userOrientation", null);

				Transaction transaction = serviceSettings.new Transaction();

				oldUserOrientation.remove(transaction);

				if (serviceSettings.autoRotate().hasValue() && oldUserOrientation.hasValue() && serviceSettings.autoRotate().get())
					//Not migrating this value when in locked orientation mode since it depends on the device orientation,
					// which we don't have, and the startup logic should be able to correctly calculate the new value anyway.
					serviceSettings.screenOrientationOffset().enqueue(Orientation.NATURAL.minus(oldUserOrientation.get()), transaction);

				transaction.commit();
			}
		});

		migrations.put(47, new Migration(null) {
			@Override
			public void upgradeData() {
				super.upgradeData();

				ApplicationSettings applicationSettings = ApplicationSettings.local();
				Setting<Boolean> inferUserWhenDeviceLocked = applicationSettings.new Setting<Boolean>(new BooleanSetting(), "inferUserWhenDeviceLocked", false);
				inferUserWhenDeviceLocked.remove();
			}
		});

		return migrations;
	}
}
