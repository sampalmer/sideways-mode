package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.HashMap;
import java.util.Map;

import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.VersionInfo;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class UpgradeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		upgradeUser(context);
		new ServiceController(context).updateFromSettings();
	}

	private void upgradeUser(Context context) {
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
				show(upgradeMessage.toString(), context);

			lastVersionUserInstructedAbout.setAndCommit(newAppVersion);
		}
	}

	private void show(String upgradeMessage, Context context) {
		Notification notification = NotificationBuilderCompat.create(context)
				.smallIcon.set(R.drawable.ic_update)
				.contentIntent.set(makeShowMessage(upgradeMessage, context))
				.contentTitle.set(context.getString(R.string.upgraded_title))
				.contentText.set(context.getString(R.string.touch_for_details))
				.autoCancel.set(true)
				.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
				.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
				.build();

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

		notificationManager.notify(NotificationIds.UPGRADE, notification);
	}

	private PendingIntent makeShowMessage(String message, Context context) {
		Intents intents = new Intents(context, DialogActivity.class);
		Intent intent = intents.makeIntent()
				.putExtra(DialogActivity.INTENT_EXTRA_MESSAGE, message)
				.putExtra(DialogActivity.INTENT_EXTRA_TITLE, R.string.upgraded_title);
		return intents.makePending(intent);
	}

	private static final Map<Integer, Integer> upgradeMessages;

	static {
		upgradeMessages = new HashMap<>();
		upgradeMessages.put(18, R.string.v18_upgrade);
	}

}
