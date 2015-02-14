package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import java.util.HashMap;
import java.util.Map;

import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.Settings;
import samuelpalmer.sensorautorotation.VersionInfo;

public class UpgradeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		upgradeUser(context);
		new ServiceController(context).updateFromSettings();
	}

	private void upgradeUser(Context context) {
		int newAppVersion = new VersionInfo(context).installedVersionCode();
		Settings.Setting<Integer> lastVersionUserInstructedAbout = new Settings(context).lastVersionUserInstructedAbout();
		Integer lastVersionUserInstructedAboutValue = lastVersionUserInstructedAbout.getOr(null);
		if (lastVersionUserInstructedAboutValue != null) {
			StringBuilder upgradeMessage = new StringBuilder();
			for (int version = lastVersionUserInstructedAboutValue + 1; version <= newAppVersion; ++version) {
				String currentMessage = context.getString(upgradeMessages.get(version));
				if (currentMessage != null) {
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
		Notification.Builder builder = new Notification.Builder(context)
				.setSmallIcon(R.drawable.ic_update)
				.setContentIntent(makeShowMessage(upgradeMessage, context))
				.setContentTitle(context.getString(R.string.upgraded_title))
				.setContentText(context.getString(R.string.touch_for_details))
				.setAutoCancel(true);

		if (Build.VERSION.SDK_INT >= 16)
			builder.setPriority(Notification.PRIORITY_HIGH);

		@SuppressWarnings("deprecation")
		Notification notification = builder.getNotification();

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

		notificationManager.notify(0, notification);
	}

	private PendingIntent makeShowMessage(String message, Context context) {
		Intents intents = new Intents(context);
		Intent intent = intents.makeIntent(UpgradeActivity.class)
				.putExtra(DialogActivity.MESSAGE_INTENT_EXTRA_NAME, message);
		return intents.makePending(intent, Intents.ComponentType.ACTIVITY);
	}

	private static final Map<Integer, Integer> upgradeMessages;

	static {
		upgradeMessages = new HashMap<>();
		upgradeMessages.put(18, R.string.v18_upgrade);
	}

}
