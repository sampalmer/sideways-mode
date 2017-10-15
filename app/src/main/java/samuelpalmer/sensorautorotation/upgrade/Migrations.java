package samuelpalmer.sensorautorotation.upgrade;

import android.content.Intent;
import android.os.Build;
import android.util.SparseArray;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.Rotation;
import samuelpalmer.common.settings.application.BooleanSetting;
import samuelpalmer.common.settings.application.OrientationSetting;
import samuelpalmer.common.settings.application.RotationSetting;
import samuelpalmer.common.settings.application.Settings.Setting;
import samuelpalmer.common.settings.application.Settings.Transaction;
import samuelpalmer.common.settings.application.StringSetting;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.processes.ui.preferences.SendConfigReceiver;

class Migrations {
	static SparseArray<Migration> buildMap() {
		SparseArray<Migration> migrations = new SparseArray<>();

		migrations.put(25, new Migration() {
			@Override
			public void upgradeUiProcessData(ApplicationSettings settings) {
				super.upgradeUiProcessData(settings);
				
				Setting<Boolean> oldAutoRotate = settings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Setting<Orientation> oldUserOrientation = settings.new Setting<Orientation>(new OrientationSetting(), "userOrientation", null);
				Setting<String> screenOffBehaviour = settings.new Setting<String>(new StringSetting(), "screenOnBehaviour", null);
				
				// Not migrating these settings anywhere since the UI process (which owns the app settings) can't directly access the service process's settings as of v57.
				
				Transaction deleteTransaction = settings.new Transaction();
				oldAutoRotate.remove(deleteTransaction);
				oldUserOrientation.remove(deleteTransaction);
				screenOffBehaviour.remove(deleteTransaction);
				deleteTransaction.commit();
			}
		});

		migrations.put(43, new Migration() {
			@Override
			public void upgradeServiceProcessData(ServiceSettings settings) {
				super.upgradeServiceProcessData(settings);
				
				Setting<Orientation> oldUserOrientation = settings.new Setting<Orientation>(new OrientationSetting(), "userOrientation", null);
				Setting<Boolean> autoRotate = settings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Setting<Rotation> screenOrientationOffset = settings.new Setting<Rotation>(new RotationSetting(), "screenOrientationOffset", null);
				
				Transaction transaction = settings.new Transaction();

				oldUserOrientation.remove(transaction);

				if (autoRotate.hasValue() && oldUserOrientation.hasValue() && autoRotate.get())
					//Not migrating this value when in locked orientation mode since it depends on the device orientation,
					// which we don't have, and the startup logic should be able to correctly calculate the new value anyway.
					screenOrientationOffset.enqueue(Orientation.NATURAL.minus(oldUserOrientation.get()), transaction);

				transaction.commit();
			}
		});

		migrations.put(47, new Migration() {
			@Override
			public void upgradeUiProcessData(ApplicationSettings settings) {
				super.upgradeUiProcessData(settings);
				
				Setting<Boolean> inferUserWhenDeviceLocked = settings.new Setting<Boolean>(new BooleanSetting(), "inferUserWhenDeviceLocked", false);
				inferUserWhenDeviceLocked.remove();
			}
		});
		
		migrations.put(51, new Migration() {
			@Override
			public void upgradeUiProcessData(ApplicationSettings settings) {
				super.upgradeUiProcessData(settings);
				
				//The format of this setting has changed.
				Setting<String> lastErrorJson = settings.new Setting<String>(new StringSetting(), "lastError", null);
				lastErrorJson.remove();
			}
		});
		
		migrations.put(54, new Migration() {
			@Override
			public CharSequence getMessage(ApplicationSettings settings) {
				boolean notificationWasHidden = !showNotificationSetting(settings).get() && Build.VERSION.SDK_INT < 25 /* The setting didn't apply in Android 7.1. */;
				
				if (notificationWasHidden)
					return CurrentProcess.context().getText(R.string.upgrade_v54);
				else
					// No need to bother the user about this if they weren't using the feature.
					return null;
			}
			
			@Override
			public void upgradeUiProcessData(ApplicationSettings settings) {
				super.upgradeUiProcessData(settings);
				
				// This setting was removed since it exploited an Android OS bug, which might be considered a violation of Google Play terms regarding malicious behaviour.
				// See https://play.google.com/about/privacy-security/malicious-behavior/
				showNotificationSetting(settings).remove();
			}
			
			private Setting<Boolean> showNotificationSetting(ApplicationSettings settings) {
				return settings.new Setting<Boolean>(new BooleanSetting(), "showNotification", true);
			}
		});
		
		migrations.put(57, new Migration() {
			
			@Override
			public CharSequence getMessage(ApplicationSettings settings) {
				return CurrentProcess.context().getString(R.string.upgrade_v57, CurrentProcess.context().getText(R.string.app_name_full));
			}
			
			@Override
			public void upgradeUiProcessData(ApplicationSettings settings) {
				super.upgradeUiProcessData(settings);
				
				Setting<Boolean> allowUpsideDownScreen = settings.new Setting<Boolean>(new BooleanSetting(), "allowUpsideDownScreen", true);
				Setting<String> notificationPriority = settings.new Setting<String>(new StringSetting(), "notificationPriority", "NORMAL");
				Setting<Boolean> enabled = settings.new Setting<Boolean>(new BooleanSetting(), "enabled", false);
				Setting<String> lastErrorJson = settings.new Setting<String>(new StringSetting(), "lastError", null);
				
				Transaction transaction = settings.new Transaction();
				allowUpsideDownScreen.remove(transaction);
				notificationPriority.remove(transaction);
				enabled.remove(transaction); // This is now determined by the service process.
				lastErrorJson.remove(transaction); // This setting has been moved into the service process.
				transaction.commit();
			}
			
			@Override
			public void upgradeServiceProcessData(ServiceSettings settings) {
				super.upgradeServiceProcessData(settings);
				
				// The service process no longer has direct access to user preferences, so we'll have to ask the UI process to send over a copy.
				CurrentProcess.context().sendBroadcast(new Intent(CurrentProcess.context(), SendConfigReceiver.class));
				
				Setting<Boolean> autoRotate = settings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Setting<Rotation> screenOrientationOffset = settings.new Setting<Rotation>(new RotationSetting(), "screenOrientationOffset", null);
				
				Boolean autoRotateValue = autoRotate.tryGet();
				Orientation screenOrientationOffsetValue = screenOrientationOffset.hasValue() ? Orientation.NATURAL.plus(screenOrientationOffset.get()) : null;
				
				// We don't have access to the authoritative "enabled" setting from this process, so we'll just infer it.
				boolean wasEnabled = autoRotate.hasValue() || screenOrientationOffset.hasValue();
				
				Transaction transaction = settings.new Transaction();
				
				if (
					wasEnabled
					&&
					autoRotateValue != null
					&&
					autoRotateValue
					&&
					screenOrientationOffsetValue != null
					&&
					!screenOrientationOffsetValue.isNaturalOrUpsideDown()
				) {
					UserSide newUserSide = screenOrientationOffsetValue.equals(Orientation.LEFT_SIDE) ? UserSide.LEFT : UserSide.RIGHT;
					settings.persistentState().enqueue(newUserSide, transaction);
				}
				
				autoRotate.remove(transaction);
				screenOrientationOffset.remove(transaction);
				
				transaction.commit();
			}
		});
		
		return migrations;
	}
}
