package samuelpalmer.sensorautorotation.processes.service.screen;

import android.util.Log;

import samuelpalmer.common.Orientation;
import samuelpalmer.sensorautorotation.BuildConfig;
import samuelpalmer.sensorautorotation.CurrentProcess;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.State;
import samuelpalmer.sensorautorotation.errors.GenericError;
import samuelpalmer.sensorautorotation.processes.service.ApplicationState;
import samuelpalmer.sensorautorotation.processes.service.EnvironmentErrorManager;
import samuelpalmer.sensorautorotation.settings.system.Setting;
import samuelpalmer.sensorautorotation.settings.system.SystemSettingAccessDeniedException;
import samuelpalmer.sensorautorotation.settings.system.SystemSettingWriteException;
import samuelpalmer.sensorautorotation.settings.system.SystemSettings;

public class ScreenOrientationController extends State {

	private final SystemSettings settings;
	private final ScreenRotationMonitor screen;
	private final ApplicationState state;
	
	public ScreenOrientationController(SystemSettings settings, ScreenRotationMonitor screen, ApplicationState state) {
		this.settings = settings;
		this.screen = screen;
		this.state = state;
	}

	@Override
	protected void start() {
		if (settings.accelerometerRotation.get()) {
			Orientation realOrientation = screen.currentRotation();
			propose(realOrientation);
			
			setAndHandleErrors(settings.accelerometerRotation, false);
			propose(realOrientation); //Just in case disabling system rotation reset this.
		}
	}

	@Override
	protected void stop() {
		setAndHandleErrors(settings.accelerometerRotation, true);
		propose(Orientation.NATURAL);
	}

	public void propose(Orientation value) {
		if (value == null)
			throw new IllegalArgumentException();

		if (BuildConfig.DEBUG)
			Log.d(getClass().getSimpleName(), "Proposing " + value);

		setAndHandleErrors(settings.userRotation, value);
	}
	
	private <T> void setAndHandleErrors(Setting<T> setting, T newValue) {
		try {
			setting.set(newValue);
		} catch (SystemSettingAccessDeniedException e) {
			GenericError error = new GenericError(R.string.permission_needed_summary, CurrentProcess.context().getText(R.string.environment_error_system_setting_write_denied_message));
			EnvironmentErrorManager.report(error);
		} catch (SystemSettingWriteException e) {
			//Because the developer can't do anything about this; it seems to be a problem on the device.
			GenericError error = new GenericError(R.string.environment_error_system_setting_write_failure_summary, CurrentProcess.context().getString(R.string.environment_error_system_setting_write_failure_message, e.key, e.value));
			EnvironmentErrorManager.report(error);
		}
	}
	
}
