package samuelpalmer.sensorautorotation;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ContentProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.util.Log;

import samuelpalmer.sensorautorotation.notifications.NotificationChannels;
import samuelpalmer.sensorautorotation.processes.ui.preferences.ApplicationSettings;
import samuelpalmer.sensorautorotation.processes.ui.preferences.SendConfigReceiver;
import samuelpalmer.sensorautorotation.upgrade.Upgrader;
import samuelpalmer.sensorautorotation.utilities.CurrentProcessNameDetector;
import samuelpalmer.sensorautorotation.values.ApplicationCommand;

/**
 * Performs process startup logic, tracks the application context, and identifies which process the calling code is running in.
 */
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
			
			if (currentProcessName.equals(packageName + context().getText(R.string.process_service)))
				isServiceProcess = true; // Service process
			else if (currentProcessName.equals(packageName + context().getText(R.string.process_ui)))
				isServiceProcess = false; // UI process
			else
				throw new RuntimeException("Current process has unrecognised name: " + currentProcessName);
		}
		
		return isServiceProcess;
	}
	
	/**
	 * This seems to be called before everything else, even {@link ContentProvider#onCreate()}.
	 */
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		
		Log.i(TAG, "Process created");
		context = this;
	}
	
	/**
	 * This is effectively the entrypoint into the current process.
	 * However, this is still called after {@link ContentProvider#onCreate()}.
	 */
	@Override
	public void onCreate() {
		super.onCreate();

		//Note that crashes that occur here don't appear to prevent the rest of the application from running, at least on Android 7.1 (not sure about other OS versions)
		
		/*
		 * Must setup notification channels before doing anything else so channels will exist when notifications are posted.
		 * Doing this regardless of which process we're in since notifications can (technically) be posted from either process.
		 */
		if (Build.VERSION.SDK_INT >= 26)
			NotificationChannels.setup();
		
		// Must upgrade settings before any settings-related code runs.
		new Upgrader().upgradeIfNecessary();
		
		if (!isServiceProcess()) {
			/*
			  Automatically pushes changes to user preferences from the UI process to the service process.
			  This ensures preference changes made by the user appear to instantly take effect.
			 */
			ApplicationSettings.getSharedPreferences(this).registerOnSharedPreferenceChangeListener(forcedAppsChanged);
			
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
		}
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		// This isn't called on real devices, so there's no need to do cleanup work here.
		
		Log.i(TAG, "Process terminated");
	}
	
	// Do not use. This is just used to prevent garbage collection of the listener.
	private final SharedPreferences.OnSharedPreferenceChangeListener forcedAppsChanged = new SharedPreferences.OnSharedPreferenceChangeListener() {
		@Override
		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
			if (s.equals(ApplicationSettings.KEY_FORCED_ORIENTATION_APP_PACKAGES)) {
				Log.i(TAG, "User preferences changed. Sending to service process.");
				new ApplicationCommand(ApplicationCommand.ACTION_SYNC_CONFIG, SendConfigReceiver.getConfigToSync()).send(CurrentProcess.this);
			}
		}
	};
	
}
