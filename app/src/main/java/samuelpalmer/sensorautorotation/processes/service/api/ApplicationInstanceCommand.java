package samuelpalmer.sensorautorotation.processes.service.api;

import android.content.Intent;

import java.util.Arrays;
import java.util.HashSet;

import samuelpalmer.sensorautorotation.ServiceController;

public class ApplicationInstanceCommand {

	public static final String ACTION_CHANGE_SIDE = "change_side";
	public static final String ACTION_UPDATE_NOTIFICATION = "update_notification";
	public static final String ACTION_TURN_OFF = "turn_off";
	public static final String ACTION_SET_LEFT_SIDE = "set_left_side";
	public static final String ACTION_SET_RIGHT_SIDE = "set_right_side";
	
	private static final HashSet<String> knownActions = new HashSet<>(Arrays.asList(
			ACTION_CHANGE_SIDE,
			ACTION_UPDATE_NOTIFICATION,
			ACTION_TURN_OFF,
			ACTION_SET_LEFT_SIDE,
			ACTION_SET_RIGHT_SIDE
	));
	
	public final String action;
	public boolean toastIfServiceDisabled = true;

	public ApplicationInstanceCommand(String action) {
		if (action == null || !knownActions.contains(action))
			throw new IllegalArgumentException("Unknown action: " + action);

		this.action = action;
	}

	public Intent toIntent() {
		return ServiceController
				.intent()
				.setAction(action);
	}

	public static ApplicationInstanceCommand fromIntent(Intent intent) {
		return new ApplicationInstanceCommand(intent.getAction());
	}

}
