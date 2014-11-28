package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.StateMonitor.Listener;
import android.content.Context;
import android.telephony.TelephonyManager;

public class UserActiveState extends ExternalResource {
	
	private OrientationSensorHandler orientationListener;
	private UserErrorHandler errorHandler;
	private OrientationCalculator calculator;
	private ProximitySensorHandler proximityHandler;
	private RecalibrationController recalibrator;
	private CallStateMonitor callStateMonitor;
	
	public UserActiveState(Context context, ScreenOrientationController screenOrientationController) {
		errorHandler = new UserErrorHandler(context);
		calculator = new OrientationCalculator(context, screenOrientationController);
		recalibrator = new RecalibrationController(new RecalibrationOverlay(context, screenOrientationController, calculator));
		
		proximityHandler = new ProximitySensorHandler(context, new ProximitySensorHandler.Listener[] { calculator, recalibrator });
		
		callStateMonitor = new CallStateMonitor(context, callStateReceiver);
		
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
		callStateMonitor.ensureStarted();
		orientationListener.ensureStarted();
		recalibrator.ensureStarted();
	}

	@Override
	protected void stop() throws EnvironmentException {
		proximityHandler.ensureStopped();
		
		if (orientationListener != null)
			orientationListener.ensureStopped();
		
		callStateMonitor.ensureStopped();
		recalibrator.ensureStopped();
		calculator.ensureStopped();
	} 
	
	private final Listener<Integer> callStateReceiver = new Listener<Integer>() {
		@Override
		public void stateChanged(Integer state) {
			boolean notOnCall = state == TelephonyManager.CALL_STATE_IDLE;
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
