package samuelpalmer.sensorautorotation;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.Arrays;
import java.util.HashSet;

import samuelpalmer.sensorautorotation.processes.service.ApplicationCommandReceiver;

/**
 * Provides a way to remotely communicate with the back-end.
 */
public class ApplicationCommand {

	public static final String ACTION_LEFT_SIDE = "left_side";
	public static final String ACTION_RIGHT_SIDE = "right_side";
	public static final String ACTION_CHANGE_SIDE = "change_side";
	public static final String ACTION_TURN_OFF = "turn_off";
	public static final String ACTION_SYNC_SERVICE = "sync_service";
	
	private static final HashSet<String> knownActions = new HashSet<>(Arrays.asList(
			ACTION_LEFT_SIDE,
			ACTION_RIGHT_SIDE,
			ACTION_CHANGE_SIDE,
			ACTION_TURN_OFF,
			ACTION_SYNC_SERVICE
	));
	
	public final String action;
	
	public ApplicationCommand(String action) {
		if (action == null || !knownActions.contains(action))
			throw new IllegalArgumentException("Unknown action: " + action);

		this.action = action;
	}
	
	public ApplicationCommand(Intent intent) {
		this(intent.getAction());
	}
	
	public void send(Context context) {
		Intent intent = asIntent(context);
		context.sendBroadcast(intent);
	}
	
	public PendingIntent asPendingIntent(Context context, int requestCode) {
		Intent intent = asIntent(context);
		
		return PendingIntent.getBroadcast(
			CurrentProcess.context(),
			requestCode,
			intent,
			PendingIntentIds.PENDING_INTENT_FLAGS);
	}
	
	private Intent asIntent(Context context) {
		Intent result = new Intent(context, ApplicationCommandReceiver.class).setAction(action);
		BroadcastKillFix.apply(result);
		return result;
	}
	
	
}
