package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.StateMonitor.Listener;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import android.content.Context;
import android.os.Vibrator;

public class UserActiveState extends ExternalResource {
	
	private OrientationSensorHandler orientationListener;
	private UserErrorHandler errorHandler;
	private OrientationCalculator calculator;
	private ProximitySensorHandler proximityHandler;
	private RecalibrationController recalibrator;
	private CallStateMonitor callStateMonitor;
	private ScreenOrientationUpdater screenUpdater;
	
	public UserActiveState(Context context, ScreenOrientationController screenOrientationController) {
		errorHandler = new UserErrorHandler(context);
		proximityHandler = new ProximitySensorHandler(context);
		calculator = new OrientationCalculator(context);
		screenUpdater = new ScreenOrientationUpdater(calculator, screenOrientationController);
		
		callStateMonitor = new CallStateMonitor(context, callStateReceiver);
		
		try {
			orientationListener = new OrientationSensorHandler(context);
		}
		catch (EnvironmentException e) {
			errorHandler.handle(e);
		}
		
		RecalibrationOverlay overlay = new RecalibrationOverlay(context, screenOrientationController, calculator);
		Vibrator vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		Recalibration recalibration = new Recalibration(overlay, calculator, orientationListener, vibrator);
		recalibrator = new RecalibrationController(proximityHandler, recalibration, errorHandler);
	}
	
	@Override
	protected void start() throws EnvironmentException {
		screenUpdater.ensureStarted();
		calculator.ensureStarted();
		callStateMonitor.ensureStarted();
		orientationListener.ensureStarted();
		recalibrator.ensureStarted();
		
		orientationListener.onRotationChanged().subscribe(deviceOrientationSubscriber);
	}

	@Override
	protected void stop() throws EnvironmentException {
		orientationListener.onRotationChanged().unsubscribe(deviceOrientationSubscriber);
		
		proximityHandler.ensureStopped();
		
		if (orientationListener != null)
			orientationListener.ensureStopped();
		
		callStateMonitor.ensureStopped();
		recalibrator.ensureStopped();
		calculator.ensureStopped();
		screenUpdater.ensureStopped();		
	} 
	
	EventHandler<Orientation> deviceOrientationSubscriber = new EventHandler<Orientation>() {
		
		@Override
		public void update(Orientation event)
				throws EnvironmentException {
			calculator.setDevice(event);
		}
	};
	
	private final Listener<CallState> callStateReceiver = new Listener<CallState>() {
		@Override
		public void stateChanged(CallState state) {
			try {
				if (!state.possiblyOnCall())
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
