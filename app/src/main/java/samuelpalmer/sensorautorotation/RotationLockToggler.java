package samuelpalmer.sensorautorotation;

import android.widget.Toast;

import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class RotationLockToggler {

	private final boolean showStatusToast;
	private final CompletionCallback callback;

	public RotationLockToggler(boolean showStatusToast, CompletionCallback callback) {
		this.showStatusToast = showStatusToast;
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
		return new ApplicationSettings().enabled.get();
	}

	private void toggle() {
		//Otherwise if the service wasn't running, it will be stopped on unbind.
		new ServiceController().ensureStarted();
		new ServiceCommandRunner(command).run();
	}

	private final ServiceCommandRunner.Command command = new ServiceCommandRunner.Command() {
		public void run(ServiceApiBinder service) {
			service.toggleRotation();
			if (showStatusToast)
				service.showStatusToast();

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
