package samuelpalmer.sensorautorotation.processes.ui.preferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Set;

import samuelpalmer.sensorautorotation.values.ApplicationCommand;

/**
 * Sends the user preferences to the service process for synchronisation. This is needed because
 * Android's {@link android.content.SharedPreferences} implementation is not multi-process-safe.
 *
 * Over the lifetime of this app's development, a number of approaches have been taken to share user
 * preferences across processes. These include:
 *
 * 1. Binding from the UI process to the service process. This was fiddly to use since the UI
 * couldn't access any settings without first binding to the service process. Also, binding can
 * interfere with OS prioritisation of a process.
 * 2. Using semaphors with {@link Context#MODE_MULTI_PROCESS}. This worked well, but the
 * {@link Context#MODE_MULTI_PROCESS} flag has since been deprecated.
 * 3. Using a {@link android.content.ContentProvider} to host the preferences. This generally worked
 * but also had numerous crashes in Production that appeared to be caused by OS bugs. It seems
 * that {@link android.content.ContentProvider}s aren't reliable in practice.
 * 4. Hosting settings in the UI process and broadcasting them to the service process. Broadcasts
 * might not be guaranteed by the OS, but ime will tell whether this is a good option.
 */
public class SendConfigReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		new ApplicationCommand(ApplicationCommand.ACTION_SYNC_CONFIG, getConfigToSync()).send(context);
	}
	
	public static Set<String> getConfigToSync() {
		return ApplicationSettings.instance().forcedOrientationAppPackages().get();
	}
	
}
