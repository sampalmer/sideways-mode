package samuelpalmer.sensorautorotation.processes.service;

import android.content.Intent;

import java.util.Arrays;
import java.util.HashSet;

import samuelpalmer.sensorautorotation.ServiceController;

public class RotationServiceCommand {

	public static final String ACTION_TOGGLE = "toggle";
	public static final String ACTION_ROTATE_LEFT = "rotate_left";
	public static final String ACTION_ROTATE_RIGHT = "rotate_right";
	public static final String ACTION_UPDATE_NOTIFICATION = "update_notification";

	private static final HashSet<String> knownActions = new HashSet<>(Arrays.asList(
			ACTION_TOGGLE,
			ACTION_ROTATE_LEFT,
			ACTION_ROTATE_RIGHT,
			ACTION_UPDATE_NOTIFICATION
	));

	private static final String EXTRA_SHOW_TOAST = "show_toast";

	final String action;
	public boolean showToast;
	public boolean toastIfServiceDisabled = true;

	public RotationServiceCommand(String action) {
		if (action == null || !knownActions.contains(action))
			throw new IllegalArgumentException("Unknown action: " + action);

		this.action = action;
	}

	public Intent toIntent() {
		return ServiceController
				.intent()
				.setAction(action)
				.putExtra(EXTRA_SHOW_TOAST, showToast);
	}

	public static RotationServiceCommand fromIntent(Intent intent) {
		if (!intent.hasExtra(EXTRA_SHOW_TOAST))
			throw new IllegalArgumentException("Intent didn't have the " + EXTRA_SHOW_TOAST + " extra");

		RotationServiceCommand result = new RotationServiceCommand(intent.getAction());
		result.showToast = intent.getBooleanExtra(EXTRA_SHOW_TOAST, false);

		return result;
	}

}
