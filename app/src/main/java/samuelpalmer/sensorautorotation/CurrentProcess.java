package samuelpalmer.sensorautorotation;

import android.app.Application;
import android.content.Context;
import android.util.Log;

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

		if (ServiceProcess.isServiceProcess())
			new Upgrader().upgradeIfNecessary();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		Log.i(TAG, "Process terminated");
	}

}
