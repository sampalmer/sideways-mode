package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

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
				String currentMessage = upgradeMessages.get(version);
				if (currentMessage != null) {
					upgradeMessage.append(currentMessage);
					upgradeMessage.append('\n');
				}
			}

			if (upgradeMessage.length() != 0)
				show(upgradeMessage.toString(), context);

			lastVersionUserInstructedAbout.set(newAppVersion);
		}
	}

	private void show(String upgradeMessage, Context context) {
		Notification notification = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(makeShowMessage(upgradeMessage, context))
				.setContentTitle(context.getString(R.string.app_name) + " Upgraded")
				.setContentText("Touch for details")
				.setAutoCancel(true)
				.setPriority(NotificationCompat.PRIORITY_HIGH)
				.build();

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Activity.NOTIFICATION_SERVICE);

		notificationManager.notify(0, notification);
	}

	private PendingIntent makeShowMessage(String message, Context context) {
		Intents intents = new Intents(context);
		Intent intent = intents.makeIntent(UpgradeActivity.class)
				.putExtra(DialogActivity.MESSAGE_INTENT_EXTRA_NAME, message);
		return intents.makePending(intent);
	}

	private static final Map<Integer, String> upgradeMessages;

	static {
		upgradeMessages = new HashMap<>();
		upgradeMessages.put(14, "Use of the proximity sensor has been removed since it is difficult to use and sometimes triggers unintentionally. Please use the notification in the notification drawer instead.");
	}

}
