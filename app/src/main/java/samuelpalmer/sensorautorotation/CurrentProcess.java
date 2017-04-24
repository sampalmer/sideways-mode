package samuelpalmer.sensorautorotation;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import samuelpalmer.sensorautorotation.processes.service.upgrade.Upgrader;

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
		
		String currentProcessName = CurrentProcessInfo.currentProcessName();
		if (currentProcessName.equals(serviceProcess()))
			new Upgrader().upgradeIfNecessary();
		else if (currentProcessName.equals(uiProcess()))
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
		else if (currentProcessName.equals(directBootProcess())) {
			//asfd
		}
		else
			throw new RuntimeException("Current process has unrecognised name: " + currentProcessName);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		Log.i(TAG, "Process terminated");
	}
	
	private String uiProcess() {
		return getPackageName();
	}
	
	private CharSequence serviceProcess() {
		return getPackageName() + StringResource.get(R.string.service_process_name);
	}
	
	private CharSequence directBootProcess() {
		return getPackageName() + StringResource.get(R.string.directboot_process_name);
	}
	
}
