package samuelpalmer.sensorautorotation;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.widget.Toast;

import samuelpalmer.sensorautorotation.components.LocalBinder;
import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.settings.application.ApplicationSettings;

public class RotationLockToggler {

	private final Context context;
	private final boolean showStatusToast;
	private final CompletionCallback callback;

	public RotationLockToggler(Context context, boolean showStatusToast, CompletionCallback callback) {
		this.context = context;
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
		return new ApplicationSettings(context).enabled().get();
	}

	private void toggle() {
		new ServiceController(context).ensureStarted();

		Intent intent = new Intent(context, RotationService.class);
		boolean bound = context.bindService(intent, connection, Context.BIND_ABOVE_CLIENT);
		if (!bound)
			throw new RuntimeException("Couldn't bind to service");
	}

	private void showNotEnabledMessage() {
		Toast
				.makeText(context, R.string.not_enabled_message, Toast.LENGTH_SHORT)
				.show();
	}

	private final ServiceConnection connection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			context.unbindService(this);

			LocalBinder localBinder = (LocalBinder)service;
			localBinder.toggleRotation();
			if (showStatusToast)
				localBinder.showStatusToast();

			callback.completed();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {}
	};

	public interface CompletionCallback {
		void completed();
	}

}
