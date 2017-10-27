package samuelpalmer.sensorautorotation.processes.service;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;

import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.errors.GenericError;
import samuelpalmer.sensorautorotation.utilities.State;

public class SystemAutoRotationMonitor extends State {
	
	private final Context context;
	private final ContentResolver contentResolver;
	
	public SystemAutoRotationMonitor(Context context) {
		this.context = context;
		contentResolver = context.getContentResolver();
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
			
			if (isStarted()) {
				boolean accelerometerRotation = Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 1;
				if (accelerometerRotation) {
					GenericError error = new GenericError(context, R.string.environment_error_system_rotation_enabled_summary, context.getText(R.string.environment_error_system_rotation_enabled_message));
					EnvironmentErrorManager.report(context, error);
				}
			}
		}
	};
	
}
