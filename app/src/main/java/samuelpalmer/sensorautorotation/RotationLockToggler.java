package samuelpalmer.sensorautorotation;

import android.widget.Toast;

import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class RotationLockToggler {

	private final CompletionCallback callback;

	public RotationLockToggler(CompletionCallback callback) {
		this.callback = callback;
	}

	public void go() {
		if (enabled())
			toggle();
		else {
			showNotEnabledMessage();
			callback.completed();
		}
	}

	private Boolean enabled() {
		return new ApplicationSettings().enabled().get();
	}

	private void toggle() {
		new ServiceCommandRunner(command).run();
	}

	private final ServiceCommandRunner.Command command = new ServiceCommandRunner.Command() {
		public void run(ServiceApiBinder service) {
			service.toggleRotation();
			callback.completed();
		}
	};

	private void showNotEnabledMessage() {
		Toast
				.makeText(ThisApplication.context(), StringResource.format(R.string.not_enabled_message, R.string.app_name), Toast.LENGTH_SHORT)
				.show();
	}

	public interface CompletionCallback {
		void completed();
	}

}
