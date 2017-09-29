package samuelpalmer.sensorautorotation.processes.service.upgrade;

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
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.settings.application.ServiceSettings;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

class Migrations {
	static SparseArray<Migration> buildMap() {
		SparseArray<Migration> migrations = new SparseArray<>();

		migrations.put(25, new Migration() {
			@Override
			public void upgradeData() {
				ApplicationSettings applicationSettings = ApplicationSettings.instance();
				Setting<Boolean> oldAutoRotate = applicationSettings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Setting<Orientation> oldUserOrientation = applicationSettings.new Setting<Orientation>(new OrientationSetting(), "userOrientation", null);
				Setting<String> screenOffBehaviour = applicationSettings.new Setting<String>(new StringSetting(), "screenOnBehaviour", null);

				ServiceSettings serviceSettings = ServiceSettings.instance();
				Setting<Orientation> newUserOrientation = serviceSettings.new Setting<Orientation>(new OrientationSetting(), "userOrientation", null);
				Setting<Boolean> newAutoRotate = serviceSettings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				
				Transaction copyTransaction = serviceSettings.new Transaction();
				copyIfPresent(oldAutoRotate, newAutoRotate, copyTransaction);
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

		migrations.put(43, new Migration() {
			@Override
			public void upgradeData() {
				super.upgradeData();

				ServiceSettings serviceSettings = ServiceSettings.instance();
				Setting<Orientation> oldUserOrientation = serviceSettings.new Setting<Orientation>(new OrientationSetting(), "userOrientation", null);
				Setting<Boolean> autoRotate = serviceSettings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Setting<Rotation> screenOrientationOffset = serviceSettings.new Setting<Rotation>(new RotationSetting(), "screenOrientationOffset", null);
				
				Transaction transaction = serviceSettings.new Transaction();

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
			public void upgradeData() {
				super.upgradeData();
				
				ApplicationSettings applicationSettings = ApplicationSettings.instance();
				Setting<Boolean> inferUserWhenDeviceLocked = applicationSettings.new Setting<Boolean>(new BooleanSetting(), "inferUserWhenDeviceLocked", false);
				inferUserWhenDeviceLocked.remove();
			}
		});
		
		migrations.put(51, new Migration() {
			@Override
			public void upgradeData() {
				super.upgradeData();
				
				//The format of this setting has changed.
				ApplicationSettings applicationSettings = ApplicationSettings.instance();
				Setting<String> lastErrorJson = applicationSettings.new Setting<String>(new StringSetting(), "lastError", null);
				lastErrorJson.remove();
			}
		});
		
		migrations.put(54, new Migration() {
			@Override
			public void upgradeData() {
				super.upgradeData();
				
				// This setting was removed since it exploited an Android OS bug, which might be considered a violation of Google Play terms regarding malicious behaviour.
				// See https://play.google.com/about/privacy-security/malicious-behavior/
				showNotificationSetting().remove();
			}
			
			@Override
			public CharSequence getMessage() {
				boolean notificationWasHidden = !showNotificationSetting().get() && Build.VERSION.SDK_INT < 25 /* The setting didn't apply in Android 7.1. */;
				
				if (notificationWasHidden)
					return StringResource.get(R.string.upgrade_v54);
				else
					// No need to bother the user about this if they weren't using the feature.
					return null;
			}
			
			private Setting<Boolean> showNotificationSetting() {
				ApplicationSettings applicationSettings = ApplicationSettings.instance();
				return applicationSettings.new Setting<Boolean>(new BooleanSetting(), "showNotification", true);
			}
		});
		
		migrations.put(57, new Migration() {
			
			@Override
			public void upgradeData() {
				super.upgradeData();
				
				ApplicationSettings applicationSettings = ApplicationSettings.instance();
				Setting<Boolean> allowUpsideDownScreen = applicationSettings.new Setting<Boolean>(new BooleanSetting(), "allowUpsideDownScreen", true);
				Setting<String> notificationPriority = applicationSettings.new Setting<String>(new StringSetting(), "notificationPriority", "NORMAL");
				Setting<Boolean> enabled = applicationSettings.new Setting<Boolean>(new BooleanSetting(), "enabled", false);
				Setting<String> lastErrorJson = applicationSettings.new Setting<String>(new StringSetting(), "lastError", null);
				String lastErrorJsonValue = lastErrorJson.tryGet();
				boolean wasEnabled = enabled.get();
				allowUpsideDownScreen.remove();
				notificationPriority.remove();
				enabled.remove();
				lastErrorJson.remove();
				
				ServiceSettings serviceSettings = ServiceSettings.instance();
				Setting<Boolean> autoRotate = serviceSettings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Boolean autoRotateValue = autoRotate.tryGet();
				if (lastErrorJsonValue != null)
					serviceSettings.lastErrorJson().set(lastErrorJsonValue);
				autoRotate.remove();
				
				Setting<Rotation> screenOrientationOffset = serviceSettings.new Setting<Rotation>(new RotationSetting(), "screenOrientationOffset", null);
				Orientation screenOrientationOffsetValue = screenOrientationOffset.hasValue() ? Orientation.NATURAL.plus(screenOrientationOffset.get()) : null;
				screenOrientationOffset.remove();
				
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
					serviceSettings.persistentState().set(newUserSide);
				}
			}
			
			@Override
			public CharSequence getMessage() {
				return StringResource.format(R.string.upgrade_v57, R.string.app_name);
			}
			
		});
		
		return migrations;
	}
}
