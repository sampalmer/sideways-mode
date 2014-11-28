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
	
	public UserActiveState(Context context, ScreenOrientationController screenOrientationController) {
		errorHandler = new UserErrorHandler(context);
		calculator = new OrientationCalculator(context, screenOrientationController);
		recalibrator = new RecalibrationController(new RecalibrationOverlay(context, screenOrientationController, calculator));
		
		proximityHandler = new ProximitySensorHandler(context, new ProximitySensorHandler.Listener[] { calculator, recalibrator });
		
		callStateMonitor = new CallStateMonitor(context, callStateReceiver);
		
		try {
			orientationListener = new OrientationSensorHandler(context);
		}
		catch (EnvironmentException e) {
			errorHandler.handle(e);
		}
		
		orientationListener.onRotationChanged().subscribe(new EventHandler<AndroidSurfaceRotation>() {
			@Override
			public void update(AndroidSurfaceRotation event) {
				calculator.setDevice(event.orientation());
			}
		});
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
