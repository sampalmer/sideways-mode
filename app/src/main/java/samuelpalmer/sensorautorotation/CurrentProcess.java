package samuelpalmer.sensorautorotation;

import android.app.Application;
import android.content.ContentProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
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
	
	@Nullable
	private static Boolean isServiceProcess;
	
	public static boolean isServiceProcess(Context context) {
		if (isServiceProcess == null) {
			String currentProcessName = CurrentProcessNameDetector.currentProcessName(context);
			String packageName = context.getPackageName();
			
			if (currentProcessName.equals(packageName + context.getText(R.string.process_service)))
				isServiceProcess = true; // Service process
			else if (currentProcessName.equals(packageName + context.getText(R.string.process_ui)))
				isServiceProcess = false; // UI process
			else
				throw new RuntimeException("Current process has unrecognised name: " + currentProcessName);
		}
		
		return isServiceProcess;
	}
	
	@Override
	protected void attachBaseContext(Context base) {
		super.attachBaseContext(base);
		
		// Application.onCreate() is called *after* ContentProvider.onCreate().
		// However, attachBaseContext() is called before it, which is why this method is used here.
		Log.i(TAG, "Process created");
	}
	
	/**
	 * This is effectively the entrypoint into the current process.
	 * However, this is still called after {@link ContentProvider#onCreate()}.
	 */
	@Override
	public void onCreate() {
		if (BuildConfig.DEBUG) {
			StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectAll()
				.permitDiskReads() // So we can read SharedPreferences and the current process name
				.permitDiskWrites() // So SharedPreferences can initialise itself on first-run
				.penaltyLog()
				.penaltyDialog()
				.build()
			);
			
			StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectAll()
				.penaltyLog()
				.penaltyDeath()
				.build()
			);
		}
		
		super.onCreate();

		//Note that crashes that occur here don't appear to prevent the rest of the application from running, at least on Android 7.1 (not sure about other OS versions)
		
		/*
		 * Must setup notification channels before doing anything else so channels will exist when notifications are posted.
		 * Doing this regardless of which process we're in since notifications can (technically) be posted from either process.
		 */
		if (Build.VERSION.SDK_INT >= 26)
			NotificationChannels.setup(this);
		
		// Must upgrade settings before any settings-related code runs.
		Upgrader.upgradeIfNecessary(this);
		
		if (!isServiceProcess(this)) {
			/*
			  Automatically pushes changes to user preferences from the UI process to the service process.
			  This ensures preference changes made by the user appear to instantly take effect.
			 */
			ApplicationSettings.getSharedPreferences(this).registerOnSharedPreferenceChangeListener(incompatibleAppsChanged);
			
			AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
		}
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		// This isn't called on real devices, so there's no need to do cleanup work here.
		
		Log.i(TAG, "Process terminated");
	}
	
	// Do not re-use. This is just stored in a field to prevent garbage collection of the listener.
	private final SharedPreferences.OnSharedPreferenceChangeListener incompatibleAppsChanged = (sharedPreferences, s) -> {
		if (s.equals(ApplicationSettings.KEY_INCOMPATIBLE_APP_PACKAGES)) {
			Log.i(TAG, "User preferences changed. Sending to service process.");
			new ApplicationCommand(ApplicationCommand.ACTION_SYNC_CONFIG, SendConfigReceiver.getConfigToSync(CurrentProcess.this)).send(CurrentProcess.this);
		}
	};
	
}
