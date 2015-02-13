package samuelpalmer.sensorautorotation;

import android.content.Context;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.components.RotationService;

public class RotationLockToggler {
	private final Context context;

	public RotationLockToggler(Context context) {
		this.context = context;
	}

	public void go() {
		if (enabled())
			toggle();
		else
			showNotEnabledMessage();
	}

	private Boolean enabled() {
		return new Settings(context).enabled().getOr(false);
	}

	private void toggle() {
		new ServiceController(context).ensureStarted(RotationService.getToggleAction(context));
	}

	private void showNotEnabledMessage() {
		Toast
				.makeText(context, R.string.not_enabled_message, Toast.LENGTH_SHORT)
				.show();
	}

}
