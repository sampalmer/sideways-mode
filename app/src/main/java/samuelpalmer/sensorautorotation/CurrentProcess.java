package samuelpalmer.sensorautorotation;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import samuelpalmer.sensorautorotation.processes.service.ApplicationInstanceManager;
import samuelpalmer.sensorautorotation.processes.service.upgrade.Upgrader;

public class CurrentProcess extends Application {

	private static final String TAG = CurrentProcess.class.getSimpleName();

	@SuppressLint("StaticFieldLeak") //Don't think this is much of a problem since it's the application context.
	private static Context context;
	
	@Nullable
	private static Boolean isServiceProcess;
	
	public static Context context() {
		if (context == null)
			throw new RuntimeException("Haven't got a context yet");

		return context;
	}
	
	public static boolean isServiceProcess() {
		if (isServiceProcess == null) {
			String currentProcessName = CurrentProcessNameDetector.currentProcessName();
			String packageName = context().getPackageName();
			
			if (currentProcessName.equals(packageName + StringResource.get(R.string.service_process_name)))
				isServiceProcess = true; // Service process
			else if (currentProcessName.equals(packageName))
				isServiceProcess = false; // UI process
			else
				throw new RuntimeException("Current process has unrecognised name: " + currentProcessName);
		}
		
		return isServiceProcess;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();

		//Note that crashes that occur here don't appear to prevent the rest of the application from running, at least on Android 7.1 (not sure about other OS versions)
		
		Log.i(TAG, "Process created");
		context = this;
		
		if (isServiceProcess()) {
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

}
