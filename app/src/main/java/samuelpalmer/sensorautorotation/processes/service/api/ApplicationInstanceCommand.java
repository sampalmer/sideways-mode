package samuelpalmer.sensorautorotation.processes.service.api;

import android.content.Intent;

import java.util.Arrays;
import java.util.HashSet;

import samuelpalmer.sensorautorotation.ServiceController;

public class ApplicationInstanceCommand {

	public static final String ACTION_ROTATE_LEFT = "rotate_left";
	public static final String ACTION_ROTATE_RIGHT = "rotate_right";
	public static final String ACTION_UPDATE_NOTIFICATION = "update_notification";

	private static final HashSet<String> knownActions = new HashSet<>(Arrays.asList(
			ACTION_ROTATE_LEFT,
			ACTION_ROTATE_RIGHT,
			ACTION_UPDATE_NOTIFICATION
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
