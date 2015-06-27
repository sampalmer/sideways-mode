package samuelpalmer.sensorautorotation.processes.service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.ui.DialogActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

class UserUpgrader {

	private final int newAppVersion;

	public UserUpgrader() {
		newAppVersion = VersionInfo.installedVersionCode();
	}

	public void upgradeUser() {
		ApplicationSettings.Setting<Integer> lastVersionUserInstructedAbout = new ApplicationSettings(Prefs.prefs()).lastVersionUserInstructedAbout();
		Integer lastAppVersion = lastVersionUserInstructedAbout.tryGet();

		//We only need to instruct the user about changes if he/she has actually run the app before.
		if (lastAppVersion != null) {
			String upgradeMessage = buildUpgradeMessage(lastAppVersion);
			if (upgradeMessage.length() != 0)
				notify(upgradeMessage);

			lastVersionUserInstructedAbout.setAndCommit(newAppVersion);
		}
	}

	private String buildUpgradeMessage(Integer lastAppVersion) {
		StringBuilder upgradeMessage = new StringBuilder();

		for (int version = lastAppVersion + 1; version <= newAppVersion; ++version) {
			Integer messageId = upgradeMessages.get(version);
			if (messageId != null) {
				CharSequence currentMessage = StringResource.get(messageId);
				upgradeMessage.append(currentMessage);
				upgradeMessage.append('\n');
			}
		}

		return upgradeMessage.toString();
	}

	private void notify(String upgradeMessage) {
		Notification notification = NotificationBuilderCompat.create()
				.setSmallIcon(R.drawable.ic_update)
				.setContentIntent(makeDialogIntent(upgradeMessage))
				.setContentTitle(messageTitle())
				.setContentText(StringResource.get(R.string.touch_for_details))
				.setAutoCancel(true)
				.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
				.build();

		SystemServices.notificationManager().notify(NotificationIds.UPGRADE, notification);
	}

	private PendingIntent makeDialogIntent(String message) {
		ComponentType componentType = ComponentType.create(DialogActivity.class);
		Intent intent = componentType.makeIntent()
				.putExtra(DialogActivity.INTENT_EXTRA_MESSAGE, message)
				.putExtra(DialogActivity.INTENT_EXTRA_TITLE, messageTitle());
		return componentType.makePending(intent);
	}

	private CharSequence messageTitle() {
		return StringResource.format(R.string.upgraded_title, R.string.app_name);
	}

	private final Map<Integer, Integer> upgradeMessages;
	{
		upgradeMessages = new HashMap<>();
		upgradeMessages.put(18, R.string.v18_upgrade);
	}

}
