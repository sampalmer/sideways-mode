package samuelpalmer.sensorautorotation.processes.service.upgrade;

import java.util.HashMap;
import java.util.Map;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.application.BooleanSetting;
import samuelpalmer.sensorautorotation.settings.application.IntegerSetting;
import samuelpalmer.sensorautorotation.settings.application.Settings.Setting;
import samuelpalmer.sensorautorotation.settings.application.Settings.Transaction;

class Migrations {
	static Map<Integer, Migration> map() {
		Map<Integer, Migration> migrations = new HashMap<>();

		migrations.put(18, new Migration(R.string.v18_upgrade));

		migrations.put(25, new Migration(null) {
			@Override
			public void upgradeData() {
				ApplicationSettings applicationSettings = ApplicationSettings.instance();
				Setting<Boolean> oldAutoRotate = applicationSettings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Setting<Integer> oldUserOrientation = applicationSettings.new Setting<Integer>(new IntegerSetting(), "userOrientation", null);

				ServiceSettings serviceSettings = new ServiceSettings();
				Transaction copyTransaction = serviceSettings.new Transaction();
				serviceSettings.autoRotate().set(oldAutoRotate.tryGet(), copyTransaction);
				serviceSettings.userOrientation().set(oldUserOrientation.tryGet(), copyTransaction);
				copyTransaction.commit();

				Transaction deleteTransaction = applicationSettings.new Transaction();
				oldAutoRotate.remove(deleteTransaction);
				oldUserOrientation.remove(deleteTransaction);
				deleteTransaction.commit();
			}
		});

		return migrations;
	}
}
