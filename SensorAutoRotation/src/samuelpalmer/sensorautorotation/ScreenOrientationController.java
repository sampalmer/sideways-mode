package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.systemconfiguration.AndroidSystemSettingSource;
import samuelpalmer.sensorautorotation.systemconfiguration.SettingWriteFailedException;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.WindowManager;

public class ScreenOrientationController extends ExternalResource {
	
	private SystemSettings settings;
	private Boolean originalAccelerometerRotation;
	private Context context;
	
	private final BroadcastReceiver configChangedReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			systemConfigChanged();
		}
		
	};
	
	public ScreenOrientationController(Context context) {
		this.context = context;
		settings = new SystemSettings(new AndroidSystemSettingSource(context.getContentResolver()));
	}
	
	@Override
	protected void start() throws EnvironmentException {
		Orientation realOrientation = realOrientation();
		propose(realOrientation);
		
		originalAccelerometerRotation = settings.accelerometerRotation.getOrNull();
		try {
			settings.accelerometerRotation.set(false);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
		
		propose(realOrientation); //Just in case disabling system rotation reset this.
		
		context.registerReceiver(configChangedReceiver, new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED));
	}

	@Override
	protected void stop() throws EnvironmentException {
		context.unregisterReceiver(configChangedReceiver);
		
		try {
			settings.userRotation.set(Orientation.NATURAL);
			settings.accelerometerRotation.set(originalAccelerometerRotation);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}
	
	public void propose(Orientation value) throws EnvironmentException {
		if (value == null)
			throw new IllegalArgumentException();
		
		try {
			settings.userRotation.set(value);
		} catch (SettingWriteFailedException e) {
			throw new EnvironmentException(e);
		}
	}
	
	public Orientation realOrientation() {
		WindowManager windows = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = windows.getDefaultDisplay().getRotation();
		return Orientation.fromAndroidSurfaceRotation(rotation);
	}
	
	public Orientation proposedOrientation() {
		Orientation result = settings.userRotation.getOrNull();
		if (result == null)
			result = Orientation.NATURAL;
		
		return result;
	}
	
	private void systemConfigChanged() {
		try {
			Boolean accelerometerRotation = settings.accelerometerRotation.getOrNull();
			if (accelerometerRotation != null && accelerometerRotation == true) {
				originalAccelerometerRotation = accelerometerRotation;
				throw new EnvironmentException("System screen auto-rotation was enabled.");
			}
		} catch (EnvironmentException e) {
			new UserErrorHandler(context).handle(e);
		}
		
		if (listener != null)
			listener.possiblyChanged();
	}
	
	@Override
	public String toString() {
		return proposedOrientation().toString();
	}
	
	private RealOrientationListener listener; 
	
	public void listen(RealOrientationListener listener) {
		if (this.listener != null)
			throw new RuntimeException("Already listening");
		
		this.listener = listener;
	}
	
	public void stopListening() {
		if (listener == null)
			throw new RuntimeException("Not listening");
		
		listener = null;
	}
	
	public interface RealOrientationListener {
		void possiblyChanged();
	}

}
