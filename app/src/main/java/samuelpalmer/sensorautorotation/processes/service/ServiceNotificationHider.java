package samuelpalmer.sensorautorotation.processes.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.Notification;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
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
/**
 * Puts {@link RotationService} in the foreground but hides its notification.
 */
public class ServiceNotificationHider extends Service {

	private final static String TAG = ServiceNotificationHider.class.getSimpleName();

	private ActivityManager activityManager;
	private ComponentName mainServiceName;
	private Settings.Setting<Boolean> showNotificationSetting;
	private boolean startCommandReceived;

	@Override
	public void onCreate() {
		activityManager = SystemServices.activityManager();
		mainServiceName = new ComponentName(this, mainService().getClass());
		showNotificationSetting = ApplicationSettings.local().showNotification();
		startCommandReceived = false;

		stopSelf();
	}

	@Override
	public void onDestroy() {
		showNotificationSetting = null;
		mainServiceName = null;
		activityManager = null;

		if (!startCommandReceived)
			throw new RuntimeException("No start command was received");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		startCommandReceived = true;

		if (ServiceHolder.isRunning() && !showNotificationSetting.get())
			applyHack();
		else
			Log.w(TAG, mainServiceName.toShortString() + " isn't running. Skipping notification removal.");

		return START_NOT_STICKY;
	}

	private void applyHack() {
		Notification notification = buildNotification();

		//Start the main service's foreground mode
		mainService().startForeground(NotificationIds.SERVICE, notification);

		try {
			//Start _this_ service's foreground mode but reuse the other's notification
			this.startForeground(NotificationIds.SERVICE, notification);
		} catch (Exception ex) {
			Log.w(TAG, new Exception("Couldn't start " + this.getClass().getSimpleName() + " in foreground. Giving up on running in foreground.", ex));
			mainService().stopForeground(true);
			return;
		}

		try {
			//Stop _this_ service's foreground mode, but cancel the other's notification
			this.stopForeground(true);
		} catch (Exception ex) {
			Log.w(TAG, new Exception("Couldn't start " + this.getClass().getSimpleName() + " in foreground. Giving up on running in foreground.", ex));

			try {
				mainService().stopForeground(true);
				//Maybe it'll work after first taking the main service out of foreground?
				this.stopForeground(true);
			} catch (Exception inner) {
				Log.w(TAG, inner);
			}

			return;
		}

		if (isForeground()) {
			Log.i(TAG, "Successfully entered foreground and removed the notification");
			//TODO: Verify that the notification was indeed hidden. Can check this in API 23 and above.
		}
		else {
			Log.w(TAG, mainService().getClass().getSimpleName() + " didn't have foreground state after trying to hide the foreground notification.");
			mainService().stopForeground(true);
		}
	}

	private Notification buildNotification() {
		return NotificationBuilderFactory.create()
				.setSmallIcon(R.drawable.rotation)
				.setContentTitle(StringResource.get(R.string.app_name))
				.setContentText(StringResource.get(R.string.notification_hiding))
				.setPriority(NotificationBuilderCompat.PRIORITY_MIN)
				.setContentIntent(ComponentType.create(MainActivity.class).pendingIntent())
				.setCategory(NotificationBuilderCompat.CATEGORY_SERVICE)
				.build();
	}

	private boolean isForeground() {
		return findMainServiceInfo().foreground;
	}

	private RunningServiceInfo findMainServiceInfo() {
		List<RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo service : services)
			if (service.service.equals(mainServiceName))
				return service;

		throw new RuntimeException("Couldn't find info for " + mainServiceName.toShortString());
	}

	private static RotationService mainService() {
		return ServiceHolder.instance();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
