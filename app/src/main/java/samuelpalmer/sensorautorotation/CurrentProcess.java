package samuelpalmer.sensorautorotation;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.service.ApplicationInstanceManager;
import samuelpalmer.sensorautorotation.processes.service.ServiceProcess;
import samuelpalmer.sensorautorotation.processes.service.upgrade.Upgrader;
import samuelpalmer.sensorautorotation.processes.ui.preferences.notifications.NotificationsActivity;

public class CurrentProcess extends Application {

	private static final String TAG = CurrentProcess.class.getSimpleName();

	@SuppressLint("StaticFieldLeak") //Don't think this is much of a problem since it's the application context.
	private static Context context;

	public static Context context() {
		if (context == null)
			throw new RuntimeException("Haven't got a context yet");

		return context;
	}

	@Override
	public void onCreate() {
		super.onCreate();

		Log.i(TAG, "Process created");
		context = this;

		/*
		 * Must setup notification channels before doing anything else so channels will exist when notifications are posted.
		 * Doing this regardless of which process we're in since notifications can (technically) be posted from either process.
		 */
		if (Build.VERSION.SDK_INT >= 26 || Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT >= 1) {
			NotificationChannels.setup();
			disableNotificationsActivity();
		}
		
		if (ServiceProcess.isServiceProcess()) {
			new Upgrader().upgradeIfNecessary();
			ApplicationInstanceManager.init();
		}
		else
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		Log.i(TAG, "Process terminated");
	}

	private void disableNotificationsActivity() {
		/*
		 * The notification preferences activity isn't relevant on Android O,
		 * but it has an intent filter that the system uses to link to it from
		 * the system's app notifications screen.
		 *
		 * This seems to be the only way to remove the above link without affecting
		 * devices on earlier Android versions.
		 */
		ComponentName notificationActivity = new ComponentName(context, NotificationsActivity.class);
		PackageManager packageManager = SystemServices.packageManager();

		if (packageManager.getComponentEnabledSetting(notificationActivity) != PackageManager.COMPONENT_ENABLED_STATE_DISABLED)
			packageManager.setComponentEnabledSetting(
					notificationActivity,
					PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
					PackageManager.DONT_KILL_APP
			);
	}

}
