package samuelpalmer.sensorautorotation;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import samuelpalmer.sensorautorotation.components.DialogActivity;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class UserUpgrader {

	private final ApplicationSettings.Setting<Integer> lastVersionUserInstructedAbout;
	private final int newAppVersion;

	public UserUpgrader() {
		lastVersionUserInstructedAbout = new ApplicationSettings().lastVersionUserInstructedAbout();
		newAppVersion = new VersionInfo().installedVersionCode();
	}

	public void upgradeUser() {
		String upgradeMessage = buildUpgradeMessage();
		if (upgradeMessage.length() != 0)
			notify(upgradeMessage);

		lastVersionUserInstructedAbout.setAndCommit(newAppVersion);
	}

	private String buildUpgradeMessage() {
		StringBuilder upgradeMessage = new StringBuilder();
		for (int version = lastVersionUserInstructedAbout.get() + 1; version <= newAppVersion; ++version) {
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
		Intents intents = new Intents(DialogActivity.class);
		Intent intent = intents.makeIntent()
				.putExtra(DialogActivity.INTENT_EXTRA_MESSAGE, message)
				.putExtra(DialogActivity.INTENT_EXTRA_TITLE, messageTitle());
		return intents.makePending(intent);
	}

	private String messageTitle() {
		return StringResource.format(R.string.upgraded_title, R.string.app_name);
	}

	private final Map<Integer, Integer> upgradeMessages;
	{
		upgradeMessages = new HashMap<>();
		upgradeMessages.put(18, R.string.v18_upgrade);
	}

}
