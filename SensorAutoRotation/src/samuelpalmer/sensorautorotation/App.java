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
	private Screen screen;
	
	public App(Context context, Screen screen) {
		this.context = context;
		this.screen = screen;
		errorHandler = new UserErrorHandler(context);
		calculator = new OrientationCalculator(context, screen);
		overlay = new RecalibrationOverlay(screen, context, calculator);
		
		proximityHandler = new ProximitySensorHandler(context, new ProximitySensorHandler.Listener[] { calculator, overlay });
		context.registerReceiver(callStateReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
		
		try {
			orientationListener = new OrientationSensorHandler(context, calculator);
			TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
			if (telephony.getCallState() == TelephonyManager.CALL_STATE_IDLE)
				proximityHandler.ensureStarted();
			orientationListener.ensureStarted();
			overlay.ensureStarted();
		}
		catch (EnvironmentException e) {
			errorHandler.handle(e);
			return;
		}
	}
	
	public void destroy() {
		if (!destroyed) {
			try {
				proximityHandler.ensureStopped();
				
				if (orientationListener != null)
					orientationListener.ensureStopped();
				
				context.unregisterReceiver(callStateReceiver);
				overlay.ensureStopped();
			
				screen.propose(Orientation.NATURAL);
			} catch (EnvironmentException e) {
				errorHandler.handle(e);
			}
			
			destroyed = true;
		}
	}
	
	private final BroadcastReceiver callStateReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
			boolean notOnCall = state.equals(TelephonyManager.EXTRA_STATE_IDLE);
			try {
				if (notOnCall)
					proximityHandler.ensureStarted();
				else {
					proximityHandler.ensureStopped();
					calculator.resetUser();
				}
			} catch (EnvironmentException e) {
				errorHandler.handle(e);
			}
		}
	}; 
}
