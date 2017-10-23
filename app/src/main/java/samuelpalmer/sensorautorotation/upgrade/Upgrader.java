package samuelpalmer.sensorautorotation.upgrade;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.Surface;

import java.util.Map;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.service.ServiceSettings;
import samuelpalmer.sensorautorotation.processes.ui.MessageDialogActivity;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.utilities.SystemSettings;

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
		SharedPreferences prefs;
		String settingsAppVersionKey;
		Integer lastAppVersion;
		
		if (CurrentProcess.isServiceProcess()) {
			prefs = ServiceSettings.getSharedPreferences(CurrentProcess.context());
			settingsAppVersionKey = ServiceSettings.KEY_APP_VERSION;
			lastAppVersion = prefs.contains(settingsAppVersionKey) ? prefs.getInt(settingsAppVersionKey, -1) : null;
			
			if (lastAppVersion == null) {
				// Either the app is newly installed or the user was using a version prior to 57, in which the app version wasn't stored in the service settings.
				lastAppVersion = guessLastAppVersion(prefs);
				if (lastAppVersion != null)
					Log.i(Upgrader.class.getSimpleName(), "Guessing that app was previously on version " + lastAppVersion);
			}
		}
		else {
			prefs = ApplicationSettings.getSharedPreferences(CurrentProcess.context());
			settingsAppVersionKey = ApplicationSettings.KEY_APP_VERSION;
			lastAppVersion = prefs.contains(settingsAppVersionKey) ? prefs.getInt(settingsAppVersionKey, -1) : null;
			
			if (lastAppVersion == null)
				// The settings app version was stored in a different setting prior to v57.
				lastAppVersion = prefs.contains("lastVersionUserInstructedAbout") ? prefs.getInt("lastVersionUserInstructedAbout", -1) : null;
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
					prefs.edit().clear().apply();
					if (!SystemSettings.canWrite(CurrentProcess.context()))
						Log.w(Migrations.class.getSimpleName(), "Didn't have permission to restore screen orientation while downgrading.");
					else {
						Settings.System.putInt(CurrentProcess.context().getContentResolver(), Settings.System.USER_ROTATION, Surface.ROTATION_0);
						// Note that it's possible for the above to fail due to DB write errors, but we'll just have to accept that and move on.
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
							migration.upgradeServiceProcessData(prefs);
						}
						else {
							// It's important that this is done *before* upgrading the data in case the data needs to be referenced.
							CharSequence message = migration.getMessage(prefs);
							if (message != null)
								upgradeMessage.append(message);
							
							migration.upgradeUiProcessData(prefs);
						}
					}
				}
				
				if (upgradeMessage.length() != 0)
					notify(upgradeMessage.toString());
				
				prefs.edit().putInt(settingsAppVersionKey, newAppVersion).apply();
			}
		}
		else if (lastAppVersion == null)
			// So that we know the settings version when we next upgrade.
			prefs.edit().putInt(settingsAppVersionKey, newAppVersion).apply();
	}
	
	/**
	 * App versions prior to 57 didn't store the app version number in the service process's preferences.
	 * This method guesses the version based on the contents of the settings.
	 */
	private Integer guessLastAppVersion(SharedPreferences serviceSettings) {
		Map<String, ?> allPreferences = serviceSettings.getAll();
		
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
		PendingIntent viewMessageIntent = MessageDialogActivity.makePendingIntent(messageTitle, upgradeMessage, PendingIntentIds.UPGRADE_MESSAGE, false);
		
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
