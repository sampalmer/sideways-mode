package samuelpalmer.sensorautorotation.values;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import samuelpalmer.sensorautorotation.processes.service.api.ApplicationCommandReceiver;
import samuelpalmer.sensorautorotation.utilities.BroadcastKillFix;

/**
 * Provides a way to remotely communicate with the back-end.
 */
public class ApplicationCommand {

	public static final String ACTION_LEFT_SIDE = "left_side";
	public static final String ACTION_RIGHT_SIDE = "right_side";
	public static final String ACTION_TURN_OFF = "turn_off";
	public static final String ACTION_CLEAR_ERROR = "clear_error";
	public static final String ACTION_SYNC_CONFIG = "sync_config";
	
	private static final HashSet<String> knownActions = new HashSet<>(Arrays.asList(
			ACTION_LEFT_SIDE,
			ACTION_RIGHT_SIDE,
			ACTION_TURN_OFF,
			ACTION_CLEAR_ERROR,
			ACTION_SYNC_CONFIG
	));
	
	public final String action;
	
	@Nullable
	public final Set<String> forcedOrientationAppPackages;
	
	public ApplicationCommand(String action) {
		this(action, null);
	}
	
	public ApplicationCommand(String action, @Nullable Set<String> forcedOrientationAppPackages) {
		if (action == null || !knownActions.contains(action))
			throw new IllegalArgumentException("Unknown action: " + action);

		this.action = action;
		this.forcedOrientationAppPackages = forcedOrientationAppPackages;
	}
	
	public ApplicationCommand(Intent intent) {
		this(intent.getAction(), getForcedOrientationAppPackages(intent));
	}
	
	private static Set<String> getForcedOrientationAppPackages(Intent intent) {
		String[] array = intent.getStringArrayExtra(null);
		if (array == null)
			return null;
		
		HashSet<String> result = new HashSet<>(array.length);
		Collections.addAll(result, array);
		return result;
	}
	
	public void send(Context context) {
		Intent intent = asIntent(context);
		context.sendBroadcast(intent);
	}
	
	public Intent asIntent(Context context) {
		// Not using startService() to deliver commands in case that was the cause of Android having multiple simultaneous instances of the service.
		Intent result = new Intent(context, ApplicationCommandReceiver.class).setAction(action);
		if (forcedOrientationAppPackages != null)
			result.putExtra(null, forcedOrientationAppPackages.toArray(new String[forcedOrientationAppPackages.size()]));
			
		BroadcastKillFix.apply(result);
		return result;
	}
	
}
