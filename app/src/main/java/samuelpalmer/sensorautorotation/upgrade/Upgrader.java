package samuelpalmer.sensorautorotation.upgrade;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.SparseArray;

import java.util.Map;

import samuelpalmer.common.Orientation;
import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.settings.application.Settings.Setting;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.ui.MessageDialogActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.settings.system.SystemSettingAccessDeniedException;
import samuelpalmer.sensorautorotation.settings.system.SystemSettingWriteException;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Upgrader {

	private final int newAppVersion;
	
	public Upgrader() {
		try {
			newAppVersion = CurrentProcess.context().getPackageManager().getPackageInfo(CurrentProcess.context().getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	public void upgradeIfNecessary() {
		Setting<Integer> settingsAppVersion;
		Integer lastAppVersion;
		
		if (CurrentProcess.isServiceProcess()) {
			settingsAppVersion = ServiceSettings.instance().appVersion();
			lastAppVersion = settingsAppVersion.tryGet();
			
			if (lastAppVersion == null) {
				// Either the app is newly installed or the user was using a version prior to 57, in which the app version wasn't stored in the service settings.
				lastAppVersion = guessLastAppVersion(ServiceSettings.instance());
				if (lastAppVersion != null)
					Log.i(Upgrader.class.getSimpleName(), "Guessing that app was previously on version " + lastAppVersion);
			}
		}
		else {
			settingsAppVersion = ApplicationSettings.instance().lastVersionUserInstructedAbout();
			lastAppVersion = settingsAppVersion.tryGet();
		}
		
		if (
			lastAppVersion != null // We only need to instruct the user about changes if he/she has actually run the app before.
			&&
			lastAppVersion != newAppVersion
		) {
			Log.i(Upgrader.class.getSimpleName(), "Upgrading " + (CurrentProcess.isServiceProcess() ? "service" : "application") + " settings from " + lastAppVersion + " to " + newAppVersion);
			
			if (lastAppVersion > newAppVersion) {
				// On my Sony Xperia M running Android 4.1.2, the OS lets me downgrade the app, so we need to guard against this.
				
				Log.w(Upgrader.class.getSimpleName(), "Warning: user is downgrading from a later version. Clearing settings.");
				
				if (CurrentProcess.isServiceProcess()) {
					ServiceSettings.instance().prefs.edit().clear().apply();
					try {
						new SystemSettings().userRotation.set(Orientation.NATURAL);
					} catch (SystemSettingAccessDeniedException e) {
						// We'll get here if the user is running Android 6 or higher due to runtime permissions.
						Log.w(Migrations.class.getSimpleName(), "Didn't have permission to restore screen orientation while downgrading.", e);
					} catch (SystemSettingWriteException e) {
						Log.w(Migrations.class.getSimpleName(), "Encountered write error when restoring the screen orientation while downgrading.", e);
					}
				}
			}
			else {
				StringBuilder upgradeMessage = new StringBuilder();
				
				SparseArray<Migration> migrations = Migrations.buildMap();
				for (int version = lastAppVersion + 1; version <= newAppVersion; ++version) {
					Migration migration = migrations.get(version);
					if (migration != null) {
						Log.i(Upgrader.class.getSimpleName(), "Running upgrade script for version " + version);
						
						if (CurrentProcess.isServiceProcess()) {
							migration.upgradeServiceProcessData(ServiceSettings.instance());
						}
						else {
							// It's important that this is done *before* upgrading the data in case the data needs to be referenced.
							CharSequence message = migration.getMessage(ApplicationSettings.instance());
							if (message != null)
								upgradeMessage.append(message);
							
							migration.upgradeUiProcessData(ApplicationSettings.instance());
						}
					}
				}
				
				if (upgradeMessage.length() != 0)
					notify(upgradeMessage.toString());
				
				settingsAppVersion.set(newAppVersion);
			}
		}
		else if (lastAppVersion == null)
			// So that we know the settings version when we next upgrade.
			settingsAppVersion.set(newAppVersion);
	}
	
	/**
	 * App versions prior to 57 didn't store the app version number in the service process's preferences.
	 * This method guesses the version based on the contents of the settings.
	 */
	private Integer guessLastAppVersion(ServiceSettings settings) {
		Map<String, ?> allPreferences = settings.prefs.getAll();
		
		if (!allPreferences.isEmpty()) {
			if (
				allPreferences.size() == 2
				&&
				allPreferences.get("autoRotate") instanceof Boolean
				&&
				allPreferences.get("screenOrientationOffset") instanceof Integer
			)
				return 43;
			else if (
				allPreferences.size() == 2
				&&
				allPreferences.get("autoRotate") instanceof Boolean
				&&
				allPreferences.get("userOrientation") instanceof Integer
			)
				return 25;
		}
		
		return null;
	}
	
	private void notify(String upgradeMessage) {
		String messageTitle = CurrentProcess.context().getString(R.string.upgrade_summary, CurrentProcess.context().getText(R.string.app_name_full));
		PendingIntent viewMessageIntent = MessageDialogActivity.makePendingIntent(messageTitle, upgradeMessage, PendingIntentIds.UPGRADE_MESSAGE);
		
		Notification notification = NotificationBuilderFactory.create()
				.setSmallIcon(R.drawable.notification_update)
				.setContentIntent(viewMessageIntent)
				.setContentTitle(messageTitle)
				.setContentText(CurrentProcess.context().getText(R.string.upgrade_notification_content))
				.setAutoCancel(true)
				.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
				.setChannelId(NotificationChannels.ID_UPGRADES)
				.build();
		
		((NotificationManager) CurrentProcess.context().getSystemService(NOTIFICATION_SERVICE)).notify(NotificationIds.UPGRADE, notification);
	}
	
}
