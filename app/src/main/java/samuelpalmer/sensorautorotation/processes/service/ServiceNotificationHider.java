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
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.NotificationIds;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

//TODO: Monitor changes to the "show notification" setting; the user might disable it while the notification is already showing
//TODO: Come up with a better name for this
/**
 * Hides the service's foreground state notification while still allowing the service to run
 * in foreground mode.
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
		//TODO: Confirm that no further notification properties are required
		Notification notification = NotificationBuilderCompat.create(this)
				.setSmallIcon(R.drawable.rotation)
				.build();

		//Start the main service's foreground mode
		mainService().startForeground(NotificationIds.SERVICE, notification);

		//Start _this_ service's foreground mode but reuse the other's notification
		//TODO: Guard against this crashing since it might not be considered valid
		this.startForeground(NotificationIds.SERVICE, notification);

		//Stop _this_ service's foreground mode, but cancel the other's notification
		//TODO: Guard against this crashing since it might not be considered valid
		this.stopForeground(true);

		if (!isForeground()) {
			Log.w(TAG, mainService().getClass().getSimpleName() + " didn't have foreground state after applying hack.");
			//TODO: Guard against this crashing since it might not be considered valid
			mainService().stopForeground(true); //Might as well cleanup properly since it didn't work
		}

		//TODO: Verify that the notification was indeed hidden: http://stackoverflow.com/questions/12654820/is-it-possible-to-check-if-a-notification-is-visible-or-canceled
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
