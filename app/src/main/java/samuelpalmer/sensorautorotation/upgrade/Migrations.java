package samuelpalmer.sensorautorotation.upgrade;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.settings.application.BooleanSetting;
import samuelpalmer.common.settings.application.IntegerSetting;
import samuelpalmer.common.settings.application.Settings.Setting;
import samuelpalmer.common.settings.application.Settings.Transaction;
import samuelpalmer.common.settings.application.StringSetting;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.UserSide;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.processes.ui.preferences.SendConfigReceiver;
import samuelpalmer.sensorautorotation.settings.system.SystemSettingAccessDeniedException;
import samuelpalmer.sensorautorotation.settings.system.SystemSettingWriteException;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

class Migrations {
	static SparseArray<Migration> buildMap() {
		SparseArray<Migration> migrations = new SparseArray<>();

		migrations.put(25, new Migration() {
			@Override
			public void upgradeUiProcessData(ApplicationSettings settings) {
				super.upgradeUiProcessData(settings);
				
				Setting<Boolean> oldAutoRotate = settings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Setting<Integer> oldUserOrientation = settings.new Setting<Integer>(new IntegerSetting(), "userOrientation", null);
				Setting<String> screenOffBehaviour = settings.new Setting<String>(new StringSetting(), "screenOnBehaviour", null);
				
				// Not migrating these settings anywhere since the UI process (which owns the app settings) can't directly access the service process's settings as of v57.
				// Instead, we'll gracefully disable the app and restore normal Android rotation.
				if (oldAutoRotate.hasValue() || oldUserOrientation.hasValue()) {
					Log.w(Migrations.class.getSimpleName(), "App was enabled, but service state cannot be migrated to service process. Disabling app.");
					
					SystemSettings systemSettings = new SystemSettings();
					
					try {
						if (oldAutoRotate.hasValue())
							systemSettings.accelerometerRotation.set(oldAutoRotate.get());
						systemSettings.userRotation.set(Orientation.NATURAL);
					} catch (SystemSettingAccessDeniedException e) {
						// We'll get here if the user is running Android 6 or higher due to runtime permissions.
						Log.w(Migrations.class.getSimpleName(), "Didn't have permission to restore the system settings while upgrading.", e);
					} catch (SystemSettingWriteException e) {
						Log.w(Migrations.class.getSimpleName(), "Encountered write error when restoring the system settings while upgrading.", e);
					}
				}
				
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
				
				Setting<Integer> oldUserOrientation = settings.new Setting<Integer>(new IntegerSetting(), "userOrientation", null);
				Setting<Boolean> autoRotate = settings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Setting<Integer> screenOrientationOffset = settings.new Setting<Integer>(new IntegerSetting(), "screenOrientationOffset", null);
				
				Transaction transaction = settings.new Transaction();

				oldUserOrientation.remove(transaction);

				if (autoRotate.hasValue() && oldUserOrientation.hasValue() && autoRotate.get())
					//Not migrating this value when in locked orientation mode since it depends on the device orientation,
					// which we don't have, and the startup logic should be able to correctly calculate the new value anyway.
					screenOrientationOffset.enqueue(Orientation.NATURAL.minus(Orientation.fromOffset(oldUserOrientation.get())).offset, transaction);

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
				Setting<Integer> lastVersionUserInstructedAbout = settings.new Setting<Integer>(new IntegerSetting(), "lastVersionUserInstructedAbout", null);
				
				
				Transaction transaction = settings.new Transaction();
				
				settings.userInstructed().enqueue(lastVersionUserInstructedAbout.hasValue(), transaction);
				allowUpsideDownScreen.remove(transaction);
				notificationPriority.remove(transaction);
				enabled.remove(transaction); // This is now determined by the service process.
				lastErrorJson.remove(transaction); // This setting has been moved into the service process.
				lastVersionUserInstructedAbout.remove(transaction);
				
				transaction.commit();
			}
			
			@Override
			public void upgradeServiceProcessData(ServiceSettings settings) {
				super.upgradeServiceProcessData(settings);
				
				// The service process no longer has direct access to user preferences, so we'll have to ask the UI process to send over a copy.
				CurrentProcess.context().sendBroadcast(new Intent(CurrentProcess.context(), SendConfigReceiver.class));
				
				Setting<Boolean> autoRotate = settings.new Setting<Boolean>(new BooleanSetting(), "autoRotate", null);
				Setting<Integer> screenOrientationOffset = settings.new Setting<Integer>(new IntegerSetting(), "screenOrientationOffset", null);
				
				Boolean autoRotateValue = autoRotate.tryGet();
				Orientation screenOrientationOffsetValue = screenOrientationOffset.hasValue() ? Orientation.fromOffset(screenOrientationOffset.get()) : null;
				
				Transaction transaction = settings.new Transaction();
				
				// We don't have access to the authoritative "enabled" setting from this process, so we'll just infer it.
				boolean wasEnabled = autoRotate.hasValue() || screenOrientationOffset.hasValue();
				if (wasEnabled) {
					if (
						autoRotateValue != null
						&&
						screenOrientationOffsetValue != null
						&&
						autoRotateValue
						&&
						(
							screenOrientationOffsetValue.equals(Orientation.LEFT_SIDE)
							||
							screenOrientationOffsetValue.equals(Orientation.RIGHT_SIDE)
						)
					) {
						UserSide newUserSide = screenOrientationOffsetValue.equals(Orientation.LEFT_SIDE) ? UserSide.LEFT : UserSide.RIGHT;
						settings.persistentState().enqueue(newUserSide, transaction);
					}
					else {
						Log.w(Migrations.class.getSimpleName(), "App was enabled, but locked mode and upright position are no longer supported. Disabling app.");
						
						// The app doesn't support locked mode or being upright anymore, so we'll gracefully disable the app.
						SystemSettings systemSettings = new SystemSettings();
						
						try {
							if (autoRotateValue != null)
								systemSettings.accelerometerRotation.set(autoRotateValue);
							systemSettings.userRotation.set(Orientation.NATURAL);
						} catch (SystemSettingAccessDeniedException e) {
							// We'll get here if the user is running Android 6 or higher due to runtime permissions.
							Log.w(Migrations.class.getSimpleName(), "Didn't have permission to restore the system settings while upgrading.", e);
						} catch (SystemSettingWriteException e) {
							Log.w(Migrations.class.getSimpleName(), "Encountered write error when restoring the system settings while upgrading.", e);
						}
					}
				}
				
				autoRotate.remove(transaction);
				screenOrientationOffset.remove(transaction);
				
				transaction.commit();
			}
		});
		
		return migrations;
	}
}
