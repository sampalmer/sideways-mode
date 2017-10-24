package samuelpalmer.sensorautorotation.processes.service;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.errors.GenericError;

public class SystemAutoRotationMonitor extends State {
	
	private final ContentResolver contentResolver;
	
	public SystemAutoRotationMonitor() {
		contentResolver = CurrentProcess.context().getContentResolver();
	}

	@Override
	protected void start() {
		contentResolver.registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), false, systemAutoRotationChanged);
	}

	@Override
	protected void stop() {
		contentResolver.unregisterContentObserver(systemAutoRotationChanged);
	}

	private final ContentObserver systemAutoRotationChanged = new ContentObserver(new Handler(Looper.getMainLooper()) /* Otherwise the callback is called in a different thread */ ) {
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			
			boolean accelerometerRotation = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 1;
			if (accelerometerRotation) {
				GenericError error = new GenericError(R.string.environment_error_system_rotation_enabled_summary, CurrentProcess.context().getText(R.string.environment_error_system_rotation_enabled_message));
				EnvironmentErrorManager.report(error);
			}
		}
	};
	
}
