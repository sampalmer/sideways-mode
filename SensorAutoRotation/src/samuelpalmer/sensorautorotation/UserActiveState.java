package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.StateMonitor.Listener;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import android.content.Context;

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
		
		Recalibration recalibration = new Recalibration(new RecalibrationOverlay(context, screenOrientationController, calculator), calculator);
		recalibrator = new RecalibrationController(proximityHandler, recalibration, errorHandler);
		
		callStateMonitor = new CallStateMonitor(context, callStateReceiver);
		
		try {
			orientationListener = new OrientationSensorHandler(context);
		}
		catch (EnvironmentException e) {
			errorHandler.handle(e);
		}
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
	
	EventHandler<AndroidSurfaceRotation> deviceOrientationSubscriber = new EventHandler<AndroidSurfaceRotation>() {
		
		@Override
		public void update(AndroidSurfaceRotation event)
				throws EnvironmentException {
			calculator.setDevice(event.orientation());
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
