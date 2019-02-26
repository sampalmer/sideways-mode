package samuelpalmer.sensorautorotation.processes.service.screen;

import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Build;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.Surface;
import androidx.annotation.Nullable;
import samuelpalmer.sensorautorotation.R;
import samuelpalmer.sensorautorotation.errors.GenericError;
import samuelpalmer.sensorautorotation.processes.service.EnvironmentErrorManager;
import samuelpalmer.sensorautorotation.utilities.AndroidSurfaceRotation;
import samuelpalmer.sensorautorotation.utilities.AppOpMonitor;
import samuelpalmer.sensorautorotation.utilities.State;
import samuelpalmer.sensorautorotation.utilities.SystemSettings;
import samuelpalmer.sensorautorotation.utilities.eventhandling.EventHandler;
import samuelpalmer.sensorautorotation.values.Orientation;

public class ScreenOrientationController extends State {
	
	private static final String TAG = ScreenOrientationController.class.getSimpleName();
	
	private final Context context;
	private final ScreenRotationMonitor screen;
	private final ContentResolver contentResolver;

	/**
	 * Will be <code>null</code> if not needed.
	 */
	@Nullable
	private final AppOpMonitor systemSettingPermissionMonitor;

	@Nullable
	private Orientation lastProposed;

	public ScreenOrientationController(Context context, ScreenRotationMonitor screen) {
		this.context = context;
		this.screen = screen;
		contentResolver = context.getContentResolver();

		if (Build.VERSION.SDK_INT >= 23)
			systemSettingPermissionMonitor = new AppOpMonitor(context, AppOpsManager.OPSTR_WRITE_SETTINGS);
		else
			systemSettingPermissionMonitor = null;
	}

	@Override
	protected void start() {
		lastProposed = null;
		
		if (Settings.System.getInt(contentResolver, Settings.System.ACCELEROMETER_ROTATION, 1) == 1) {
			Orientation realOrientation = screen.currentRotation();
			propose(realOrientation);
			
			setAndHandleErrors(Settings.System.ACCELEROMETER_ROTATION, 0);
			propose(realOrientation); //Just in case disabling system rotation reset this.
		}
		
		contentResolver.registerContentObserver(Settings.System.getUriFor(Settings.System.USER_ROTATION), false, onUserRotationChanged);

		if (systemSettingPermissionMonitor != null)
			systemSettingPermissionMonitor.subscribe(systemSettingPermissionChanged);
	}

	@Override
	protected void stop() {
		if (systemSettingPermissionMonitor != null)
			systemSettingPermissionMonitor.unsubscribe(systemSettingPermissionChanged);

		contentResolver.unregisterContentObserver(onUserRotationChanged);
		setAndHandleErrors(Settings.System.ACCELEROMETER_ROTATION, 1);
		propose(Orientation.NATURAL);
	}

	public void propose(Orientation value) {
		if (value == null)
			throw new IllegalArgumentException();

		Log.i(TAG, "Proposing screen orientation: " + value);
		
		boolean worked = setAndHandleErrors(Settings.System.USER_ROTATION, AndroidSurfaceRotation.fromOrientationClockwise(value));
		if (worked)
			lastProposed = value;
	}
	
	private boolean setAndHandleErrors(String name, int newValue) {
		if (!validateSystemSettingsPermission())
			return false;

		boolean worked = Settings.System.putInt(contentResolver, name, newValue);
		if (!worked) {
			//Because the developer can't do anything about this; it seems to be a problem on the device.
			GenericError error = new GenericError(context, R.string.environment_error_system_setting_write_failure_summary, context.getString(R.string.environment_error_system_setting_write_failure_message, name, Integer.toString(newValue)));
			EnvironmentErrorManager.report(context, error);
			return false;
		}
		
		return true;
	}

	private final ContentObserver onUserRotationChanged = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			
			if (isStarted() && lastProposed != null) {
				int newUserRotation = Settings.System.getInt(contentResolver, Settings.System.USER_ROTATION, Surface.ROTATION_0);
				Orientation newOrientation = AndroidSurfaceRotation.toOrientationClockwise(newUserRotation);
				if (!newOrientation.equals(lastProposed)) {
					/*
					 * Android P sometimes changes the value of USER_ROTATION to match the current screen orientation.
					 * This can be problematic, since the current screen orientation might be the result of an open app's
					 * orientation preferences. For example, when the launcher is open, the screen orientation is usually portrait.
					 *
					 * That breaks this app, so we'll just have to fight with the OS and change it back.
					 */
					Log.i(TAG, "Something proposed orientation " + newOrientation + ". Reverting to " + lastProposed + ".");
					setAndHandleErrors(Settings.System.USER_ROTATION, AndroidSurfaceRotation.fromOrientationClockwise(lastProposed));
				}
			}
		}
	};

	private final EventHandler systemSettingPermissionChanged = this::validateSystemSettingsPermission;

	/**
	 * Raises an {@link samuelpalmer.sensorautorotation.errors.EnvironmentError} if system settings permission is missing.
	 * @return Whether the permission is granted
	 */
	private boolean validateSystemSettingsPermission() {
		if (!SystemSettings.canWrite(context)) {
			GenericError error = new GenericError(context, R.string.permission_needed_summary, context.getString(R.string.environment_error_system_setting_write_denied_message, context.getText(R.string.app_name_full)));
			EnvironmentErrorManager.report(context, error);
			return false;
		}
		else
			return true;
	}

}
