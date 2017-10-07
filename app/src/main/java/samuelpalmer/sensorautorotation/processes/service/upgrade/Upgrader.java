package samuelpalmer.sensorautorotation.processes.service.upgrade;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.util.SparseArray;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.settings.application.Settings.Setting;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.ui.MessageDialogActivity;
import samuelpalmer.sensorautorotation.settings.ApplicationSettings;

import static android.content.Context.NOTIFICATION_SERVICE;

public class Upgrader {

	private final int newAppVersion;
	private final Setting<Integer> lastVersionUserInstructedAbout;
	private final Integer lastAppVersion;

	public Upgrader() {
		try {
			newAppVersion = CurrentProcess.context().getPackageManager().getPackageInfo(CurrentProcess.context().getPackageName(), 0).versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
		
		lastVersionUserInstructedAbout = ApplicationSettings.instance().lastVersionUserInstructedAbout();
		lastAppVersion = lastVersionUserInstructedAbout.tryGet();
	}

	public void upgradeIfNecessary() {
		if (
			lastAppVersion != null // We only need to instruct the user about changes if he/she has actually run the app before.
			&&
			lastAppVersion != newAppVersion
		) {
			Log.i(Upgrader.class.getSimpleName(), "Upgrading from " + lastAppVersion + " to " + newAppVersion);
			upgrade();
		}
	}

	private void upgrade() {
		StringBuilder upgradeMessage = new StringBuilder();

		SparseArray<Migration> migrations = Migrations.buildMap();
		for (int version = lastAppVersion + 1; version <= newAppVersion; ++version) {
			Migration migration = migrations.get(version);
			if (migration != null) {
				Log.i(Upgrader.class.getSimpleName(), "Running upgrade script for version " + version);
				
				// It's important that this is done *before* upgrading the data in case the data needs to be referenced.
				CharSequence message = migration.getMessage();
				if (message != null)
					upgradeMessage.append(message);
				
				migration.upgradeData();
			}
		}

		if (upgradeMessage.length() != 0)
			notify(upgradeMessage.toString());

		lastVersionUserInstructedAbout.set(newAppVersion);
	}

	private void notify(String upgradeMessage) {
		Notification notification = NotificationBuilderFactory.create()
				.setSmallIcon(R.drawable.notification_update)
				.setContentIntent(makeDialogIntent(upgradeMessage))
				.setContentTitle(messageTitle())
				.setContentText(CurrentProcess.context().getText(R.string.upgrade_notification_content))
				.setAutoCancel(true)
				.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
				.setChannelId(NotificationChannels.ID_UPGRADES)
				.build();
		
		((NotificationManager) CurrentProcess.context().getSystemService(NOTIFICATION_SERVICE)).notify(NotificationIds.UPGRADE, notification);
	}

	private PendingIntent makeDialogIntent(String message) {
		return MessageDialogActivity.makePendingIntent(messageTitle(), message, PendingIntentIds.UPGRADE_MESSAGE);
	}

	private CharSequence messageTitle() {
		return CurrentProcess.context().getString(R.string.upgrade_summary, CurrentProcess.context().getText(R.string.app_name));
	}

}
