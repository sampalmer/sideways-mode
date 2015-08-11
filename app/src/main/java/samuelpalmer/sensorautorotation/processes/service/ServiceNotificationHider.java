package samuelpalmer.sensorautorotation.processes.service;

import android.app.ActivityManager.RunningServiceInfo;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import java.util.List;

import samuelpalmer.sensorautorotation.EnvironmentError;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.SystemServices;
import samuelpalmer.sensorautorotation.notifications.service.NotificationServiceStatusDisplay;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

/**
 * Hides the service's foreground state notification while still allowing the service to run
 * in foreground mode.
 */
public class ServiceNotificationHider extends Service {

	@Override
	public void onCreate() {
		super.onCreate();

		try {
			applyHack();
		}
		catch (NotificationException ex) {
			Log.w(ServiceNotificationHider.class.getSimpleName(), ex);
			EnvironmentError.report(R.string.notification_hiding_failed_summary, R.string.notification_hiding_failed_message);
		}

		stopSelf();
	}

	private void applyHack() {
		ApplicationSettings settings = new ApplicationSettings();

		mainService().serviceStatusGenerator.update(new NotificationServiceStatusDisplay(mainService(), settings));

		if (!isForeground())
			throw new NotificationException(mainService().getClass().getSimpleName() + " didn't enter foreground mode.");

		//Start _this_ service's foreground mode but reuse the other's notification
		NotificationServiceStatusDisplay thisServiceNotification = new NotificationServiceStatusDisplay(this, settings);
		mainService().serviceStatusGenerator.update(thisServiceNotification);

		//Stop _this_ service's foreground mode, but cancel the other's notification
		thisServiceNotification.cancel();

		if (!isForeground())
			throw new NotificationException(mainService().getClass().getSimpleName() + " lost foreground state after applying hack.");
	}

	private boolean isForeground() {
		return findMainServiceInfo().foreground;
	}

	private RunningServiceInfo findMainServiceInfo() {
		ComponentName mainServiceComponentName = new ComponentName(this, mainService().getClass());

		List<RunningServiceInfo> services = SystemServices.activityManager().getRunningServices(Integer.MAX_VALUE);
		for (RunningServiceInfo service : services)
			if (service.service.equals(mainServiceComponentName))
				return service;

		throw new RuntimeException("Couldn't find info for " + mainServiceComponentName.toShortString());
	}

	private RotationService mainService() {
		return ServiceHolder.instance();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_NOT_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

}
