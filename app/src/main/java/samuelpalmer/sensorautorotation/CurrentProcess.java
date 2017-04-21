package samuelpalmer.sensorautorotation;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.service.ServiceProcess;
import samuelpalmer.sensorautorotation.processes.service.upgrade.Upgrader;

public class CurrentProcess extends Application {

	private static final String TAG = CurrentProcess.class.getSimpleName();
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
		if (Build.VERSION.SDK_INT >= 26 || Build.VERSION.SDK_INT == 25 && Build.VERSION.PREVIEW_SDK_INT >= 1)
			NotificationChannels.setup();

		if (ServiceProcess.isServiceProcess())
			new Upgrader().upgradeIfNecessary();
		else
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		Log.i(TAG, "Process terminated");
	}

}
