package samuelpalmer.sensorautorotation.processes.service.screen;

import android.provider.Settings;
import android.util.Log;

import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.errors.GenericError;
import samuelpalmer.sensorautorotation.processes.service.EnvironmentErrorManager;
import samuelpalmer.sensorautorotation.utilities.SystemSettings;
import samuelpalmer.sensorautorotation.utilities.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.values.Orientation;

public class ScreenOrientationController extends State {
	
	private static final String TAG = ScreenOrientationController.class.getSimpleName();
	
	private final ScreenRotationMonitor screen;
	
	public ScreenOrientationController(ScreenRotationMonitor screen) {
		this.screen = screen;
	}

	@Override
	protected void start() {
		if (Settings.System.getInt(CurrentProcess.context().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 1) == 1) {
			Orientation realOrientation = screen.currentRotation();
			propose(realOrientation);
			
			setAndHandleErrors(Settings.System.ACCELEROMETER_ROTATION, 0);
			propose(realOrientation); //Just in case disabling system rotation reset this.
		}
	}

	@Override
	protected void stop() {
		setAndHandleErrors(Settings.System.ACCELEROMETER_ROTATION, 1);
		propose(Orientation.NATURAL);
	}

	public void propose(Orientation value) {
		if (value == null)
			throw new IllegalArgumentException();

		Log.i(TAG, "Proposing screen orientation: " + value);
		
		setAndHandleErrors(Settings.System.USER_ROTATION, AndroidSurfaceRotation.fromOrientationClockwise(value));
	}
	
	private void setAndHandleErrors(String name, int newValue) {
		if (!SystemSettings.canWrite(CurrentProcess.context())) {
			GenericError error = new GenericError(R.string.permission_needed_summary, CurrentProcess.context().getText(R.string.environment_error_system_setting_write_denied_message));
			EnvironmentErrorManager.report(error);
		}
		else {
			boolean worked = Settings.System.putInt(CurrentProcess.context().getContentResolver(), name, newValue);
			if (!worked) {
				//Because the developer can't do anything about this; it seems to be a problem on the device.
				GenericError error = new GenericError(R.string.environment_error_system_setting_write_failure_summary, CurrentProcess.context().getString(R.string.environment_error_system_setting_write_failure_message, name, Integer.toString(newValue)));
				EnvironmentErrorManager.report(error);
			}
		}
	}
	
}
