package samuelpalmer.sensorautorotation.processes.ui;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import samuelpalmer.sensorautorotation.PendingIntentIds;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderCompat;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.upgrade.Upgrader;

/**
 * Posts a notification to inform the user about an upgrade from a previous version of this app.
 * This is specifically done when the {@link Intent#ACTION_MY_PACKAGE_REPLACED} intent is received
 * because that guarantees that this is a genuine, direct app upgrade situation.<br>
 * <br>
 * It's also possible for the app settings to be from a previous version of the app without the user
 * directly upgrading. In those situations, we don't want to bother the user with an upgrade message
 * because that doesn't seem relevant to the user.<br>
 * <br>
 * For example:
 * <ol>
 * <li>User has a previous version of the app installed</li>
 * <li>Android backs up the app's settings to the cloud</li>
 * <li>User uninstalls the app or changes device</li>
 * <li>User installs the current version of the app</li>
 * <li>Android restores the settings of the previous version of the app from the cloud</li>
 * </ol>
 */
public class ShowUpgradeMessageReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(ShowUpgradeMessageReceiver.class.getSimpleName(), "Received intent: " + intent.getAction());
		
		if (!Intent.ACTION_MY_PACKAGE_REPLACED.equals(intent.getAction())) {
			Log.w(ShowUpgradeMessageReceiver.class.getSimpleName(), "Unknown action: " + intent.getAction());
			return;
		}
		
		Integer lastAppVersion = Upgrader.getLastAppVersion();
		if (lastAppVersion == null) {
			// This is the first time the app has been run, so there's no need to show an upgrade message.
			return;
		}
		
		String upgradeMessage = makeUpgradeMessage(context, lastAppVersion);
		if (upgradeMessage != null)
			postUpgradeNotification(context, upgradeMessage);
	}
	
	@Nullable
	private String makeUpgradeMessage(Context context, int lastAppVersion) {
		if (lastAppVersion < 57)
			return context.getString(R.string.upgrade_v57, context.getText(R.string.app_name_full));
		else
			return null;
	}
	
	private static void postUpgradeNotification(Context context, String upgradeMessage) {
		String messageTitle = context.getString(R.string.upgrade_summary, context.getText(R.string.app_name_full));
		PendingIntent viewMessageIntent = MessageDialogActivity.makePendingIntent(context, messageTitle, upgradeMessage, PendingIntentIds.UPGRADE_MESSAGE, false);
		
		Notification notification = NotificationBuilderFactory.create(context)
			.setSmallIcon(R.drawable.notification_update)
			.setContentIntent(viewMessageIntent)
			.setContentTitle(messageTitle)
			.setContentText(context.getText(R.string.upgrade_notification_content))
			.setAutoCancel(true)
			.setPriority(NotificationBuilderCompat.PRIORITY_HIGH)
			.setCategory(NotificationBuilderCompat.CATEGORY_STATUS)
			.setChannelId(NotificationChannels.ID_UPGRADES)
			.build();

		((NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE)).notify(NotificationIds.UPGRADE, notification);
	}
	
}
