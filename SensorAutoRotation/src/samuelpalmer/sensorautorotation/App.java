package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

public class App {
	
	private OrientationSensorHandler orientationListener;
	private UserErrorHandler errorHandler;
	private boolean destroyed;
	private OrientationCalculator calculator;
	private ProximitySensorHandler proximityHandler;
	private RecalibrationOverlay overlay;
	private Context context;
	
	public App(Context context, Screen screen) {
		this.context = context;
		errorHandler = new UserErrorHandler(context);
		calculator = new OrientationCalculator(context, screen);
		overlay = new RecalibrationOverlay(screen, context);
		context.registerReceiver(callStateReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
		
		try {
			orientationListener = new OrientationSensorHandler(context, calculator);
			TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephony.getCallState() == TelephonyManager.CALL_STATE_IDLE)
				startProximityMonitoring();
		}
		catch (EnvironmentException e) {
			errorHandler.handle(e);
			return;
		}
		
		orientationListener.enable();
	}
	
	public void destroy() {
		if (!destroyed) {
			stopProximityMonitoring();
			
			if (orientationListener != null)
				orientationListener.disable();
			
			context.unregisterReceiver(callStateReceiver);
			overlay.stop();
			
			destroyed = true;
		}
	}
	
	private final BroadcastReceiver callStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			if (state.equals(TelephonyManager.EXTRA_STATE_IDLE))
				try {
					startProximityMonitoring();
				} catch (EnvironmentException e) {
					errorHandler.handle(e);
				}
			else {
				stopProximityMonitoring();
				calculator.resetUser();
			}
		}
	}; 
	
	private void startProximityMonitoring() throws EnvironmentException {
		if (proximityHandler == null)
			proximityHandler = new ProximitySensorHandler(context, new ProximitySensorHandler.Listener[] { calculator, overlay });
	}
	
	private void stopProximityMonitoring() {
		if (proximityHandler != null) {
			proximityHandler.stop();
			proximityHandler = null;
		}
	}
}
