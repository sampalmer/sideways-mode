package samuelpalmer.sensorautorotation;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

public class UserActiveState extends ExternalResource {
	
	private OrientationSensorHandler orientationListener;
	private UserErrorHandler errorHandler;
	private OrientationCalculator calculator;
	private ProximitySensorHandler proximityHandler;
	private RecalibrationController recalibrator;
	private Context context;
	
	public UserActiveState(Context context, ScreenOrientationController screenOrientationController) {
		this.context = context;
		errorHandler = new UserErrorHandler(context);
		calculator = new OrientationCalculator(context, screenOrientationController);
		recalibrator = new RecalibrationController(new RecalibrationOverlay(context, screenOrientationController, calculator));
		
		proximityHandler = new ProximitySensorHandler(context, new ProximitySensorHandler.Listener[] { calculator, recalibrator });
		
		try {
			orientationListener = new OrientationSensorHandler(context, calculator);
		}
		catch (EnvironmentException e) {
			errorHandler.handle(e);
		}
	}
	
	@Override
	protected void start() throws EnvironmentException {
		calculator.ensureStarted();
		
		context.registerReceiver(callStateReceiver, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));
		TelephonyManager telephony = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		if (telephony.getCallState() == TelephonyManager.CALL_STATE_IDLE)
			proximityHandler.ensureStarted();
		
		orientationListener.ensureStarted();
		recalibrator.ensureStarted();
	}

	@Override
	protected void stop() throws EnvironmentException {
		proximityHandler.ensureStopped();
		
		if (orientationListener != null)
			orientationListener.ensureStopped();
		
		context.unregisterReceiver(callStateReceiver);
		recalibrator.ensureStopped();
		calculator.ensureStopped();
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
