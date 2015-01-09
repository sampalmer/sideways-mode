package samuelpalmer.sensorautorotation.components;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.v4.app.NotificationCompat;

import java.util.HashMap;
import java.util.Map;

import samuelpalmer.sensorautorotation.Intents;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.ServiceController;
import samuelpalmer.sensorautorotation.Settings;

public class UpgradeReceiver extends BroadcastReceiver {

	public static final int VERSION_BEFORE_UPGRADE_MESSAGES = 13;

	@Override
	public void onReceive(Context context, Intent intent) {
		int newAppVersion = currentVersionCode(context);
		Settings.Setting<Integer> lastVersionUserInstructedAbout = new Settings(context).lastVersionUserInstructedAbout();
		int lastVersionUserInstructedAboutValue = lastVersionUserInstructedAbout.getOr(VERSION_BEFORE_UPGRADE_MESSAGES);

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

		new ServiceController(context).updateFromSettings();
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

	private int currentVersionCode(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (PackageManager.NameNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static final Map<Integer, String> upgradeMessages;

	static {
		upgradeMessages = new HashMap<>();
		upgradeMessages.put(14, "Use of the proximity sensor has been removed since it is difficult to use and sometimes triggers unintentionally. Please use the notification in the notification drawer instead.");
	}

}
