package samuelpalmer.sensorautorotation;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import samuelpalmer.sensorautorotation.components.DialogActivity;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class UserUpgrader {

	private final Context context;

	public UserUpgrader(Context context) {
		this.context = context;
	}

	public void upgradeUser() {
		int newAppVersion = new VersionInfo(context).installedVersionCode();
		ApplicationSettings.Setting<Integer> lastVersionUserInstructedAbout = new ApplicationSettings(context).lastVersionUserInstructedAbout();
		Integer lastVersionUserInstructedAboutValue = lastVersionUserInstructedAbout.tryGet();
		if (lastVersionUserInstructedAboutValue != null) {
			StringBuilder upgradeMessage = new StringBuilder();
			for (int version = lastVersionUserInstructedAboutValue + 1; version <= newAppVersion; ++version) {
				Integer messageId = upgradeMessages.get(version);
				if (messageId != null) {
					String currentMessage = context.getString(messageId);
					upgradeMessage.append(currentMessage);
					upgradeMessage.append('\n');
				}
			}

			if (upgradeMessage.length() != 0)
				notify(upgradeMessage.toString());

			lastVersionUserInstructedAbout.setAndCommit(newAppVersion);
		}
	}

	private void notify(String upgradeMessage) {
		Notification notification = NotificationBuilderCompat.create(context)
				.setSmallIcon(R.drawable.ic_update)
				.setContentIntent(makeDialogIntent(upgradeMessage))
				.setContentTitle(title())
				.setContentText(context.getString(R.string.touch_for_details))
				.setAutoCancel(true)
				.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
				.build();

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

		notificationManager.notify(NotificationIds.UPGRADE, notification);
	}

	private PendingIntent makeDialogIntent(String message) {
		Intents intents = new Intents(context, DialogActivity.class);
		Intent intent = intents.makeIntent()
				.putExtra(DialogActivity.INTENT_EXTRA_MESSAGE, message)
				.putExtra(DialogActivity.INTENT_EXTRA_TITLE, title());
		return intents.makePending(intent);
	}

	private String title() {
		return context.getString(R.string.upgraded_title, context.getText(R.string.app_name));
	}

	private final Map<Integer, Integer> upgradeMessages;
	{
		upgradeMessages = new HashMap<>();
		upgradeMessages.put(18, R.string.v18_upgrade);
	}

}
