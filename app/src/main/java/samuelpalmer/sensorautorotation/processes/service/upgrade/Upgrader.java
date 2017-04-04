package samuelpalmer.sensorautorotation.processes.service.upgrade;

import android.app.Notification;
import android.app.PendingIntent;
import android.util.Log;
import android.util.SparseArray;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.settings.application.Settings.Setting;
import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.ui.MessageDialogActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class Upgrader {

	private final int newAppVersion;
	private final Setting<Integer> lastVersionUserInstructedAbout;
	private final Integer lastAppVersion;

	public Upgrader() {
		newAppVersion = VersionInfo.installedVersionCode();
		lastVersionUserInstructedAbout = ApplicationSettings.local().lastVersionUserInstructedAbout();
		lastAppVersion = lastVersionUserInstructedAbout.tryGet();
	}

	public void upgradeIfNecessary() {
		//We only need to instruct the user about changes if he/she has actually run the app before.
		if (lastAppVersion != null) {
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
				upgradeMessage.append(migration.message());
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
				.setContentText(StringResource.get(R.string.upgrade_notification_content))
				.setAutoCancel(true)
				.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
				.build();

		SystemServices.notificationManager().notify(NotificationIds.UPGRADE, notification);
	}

	private PendingIntent makeDialogIntent(String message) {
		return MessageDialogActivity.makePendingIntent(messageTitle(), message, PendingIntentIds.UPGRADE_MESSAGE);
	}

	private CharSequence messageTitle() {
		return StringResource.format(R.string.upgrade_summary, R.string.app_name);
	}

}
