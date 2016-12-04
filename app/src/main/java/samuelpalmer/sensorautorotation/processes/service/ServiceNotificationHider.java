package samuelpalmer.sensorautorotation.processes.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import java.util.List;

import samuelpalmer.common.notifications.NotificationBuilderCompat;
import samuelpalmer.common.settings.application.Settings;
import samuelpalmer.sensorautorotation.ComponentType;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.StringResource;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationBuilderFactory;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.processes.ui.MainActivity;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

//TODO: This doesn't work on Android 7.1. Decide what to do on that OS. (Maybe direct users to disable the app's notifications.)
//TODO: On failure, issue an EnvironmentError and advise the user to enable the notification listener service
/**
 * Puts {@link RotationService} in the foreground but hides its notification.
 */
public class ServiceNotificationHider extends Service {

	private final static String TAG = ServiceNotificationHider.class.getSimpleName();

	private boolean startCommandReceived;

	@Override
	public void onCreate() {
		startCommandReceived = false;
	}

	@Override
	public void onDestroy() {
		if (!startCommandReceived)
			throw new RuntimeException("No start command was received");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startCommandReceived = true;

		String reasonSkipped = applyHackIfAppropriate();
		if (reasonSkipped != null)
			Log.w(TAG, reasonSkipped + " Skipped notification removal.");

		stopSelf(); //Don't want to leave the service running and waste RAM

		return START_NOT_STICKY;
	}

	/**
	 * @return The reason the hack was not applied, or {@code null}.
	 */
	private String applyHackIfAppropriate() {
		if (!ServiceHolder.isRunning())
			return RotationService.class.getSimpleName() + " isn't running.";

		Settings.Setting<Boolean> showNotificationSetting = ApplicationSettings.local().showNotification();
		if (showNotificationSetting.get())
			return showNotificationSetting.getKey() + " is true.";

		applyHack();
		return null;
	}

	private void applyHack() {
		Notification notification = buildNotification();

		//Start the main service's foreground mode
		mainService().startForeground(NotificationIds.SERVICE, notification);

		try {
			//Start _this_ service's foreground mode but reuse the other's notification
			this.startForeground(NotificationIds.SERVICE, notification);
		} catch (Exception ex) {
			Log.w(TAG, new Exception("Couldn't enter " + this.getClass().getSimpleName() + " in foreground. Giving up on running in foreground.", ex));
			mainService().stopForeground(true);
			return;
		}

		try {
			//Stop _this_ service's foreground mode, but cancel the other's notification
			this.stopForeground(true);
		} catch (Exception ex) {
			Log.w(TAG, new Exception("Couldn't remove " + this.getClass().getSimpleName() + " from foreground. Giving up on running in foreground.", ex));

			try {
				mainService().stopForeground(true);
				//Maybe it'll work after first taking the main service out of foreground?
				this.stopForeground(true);
			} catch (Exception inner) {
				Log.w(TAG, inner);
			}

			return;
		}

		Boolean notificationPresent = isNotificationPresent(NotificationIds.SERVICE);
		if (notificationPresent != null && notificationPresent) {
			Log.w(TAG, "The service notification was still present after trying to hide it. The service is probably being stopped.");
			return;
		}

		RunningServiceInfo mainServiceInfo = tryFindMainServiceInfo();
		if (mainServiceInfo == null) {
			Log.w(TAG, "Couldn't find running service info for " + RotationService.class.getSimpleName() + " after trying to hide its notification. The service is probably being stopped.");
			return;
		}

		if (!mainServiceInfo.foreground) {
			Log.w(TAG, RotationService.class.getSimpleName() + " didn't have foreground state after trying to hide the foreground notification. The service is probably being stopped.");
			return;
		}

		Log.i(TAG, "Successfully entered foreground after trying to remove the notification.");
	}

	private Boolean isNotificationPresent(int id) {
		if (Build.VERSION.SDK_INT < 23)
			return null;

		NotificationManager notificationManager = SystemServices.notificationManager();
		StatusBarNotification[] activeNotifications;

		try {
			activeNotifications = notificationManager.getActiveNotifications();
		} catch (Exception ex) {
			//This seems to  be a timing-related bug; it happened when I rapidly enabled and disabled the app on Android 6.0.1.
			Log.w(TAG, "Couldn't check if the notification was active.", ex);
			return null;
		}

		String thisPackageName = getPackageName();

		for (StatusBarNotification activeNotification : activeNotifications)
			if (activeNotification.getPackageName().equals(thisPackageName) && activeNotification.getId() == id)
				return true;

		return false;
	}

	private Notification buildNotification() {
		return NotificationBuilderFactory.create()
				.setSmallIcon(R.drawable.rotation)
				.setContentTitle(StringResource.get(R.string.app_name))
				.setContentText(StringResource.get(R.string.notification_hiding))
				.setPriority(NotificationBuilderCompat.PRIORITY_MIN) //Just in case the notification is visible for a moment
				.setContentIntent(ComponentType.create(MainActivity.class).pendingIntent())
				.setCategory(NotificationBuilderCompat.CATEGORY_SERVICE)
				.build();
	}

	private RunningServiceInfo tryFindMainServiceInfo() {
		ActivityManager activityManager = SystemServices.activityManager();
		ComponentName mainServiceName = new ComponentName(this, RotationService.class);

		List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo service : services)
			if (service.service.equals(mainServiceName))
				return service;

		return null;
	}

	private static RotationService mainService() {
		return ServiceHolder.instance();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
