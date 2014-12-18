package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.StateMonitor.Listener;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;
import android.content.Context;

public class UserActiveState extends State {
	
	private OrientationSensorHandler orientationListener;
	private UserErrorHandler errorHandler;
	private OrientationCalculator calculator;
	private ProximitySensorHandler proximityHandler;
	private RecalibrationController recalibrator;
	private EarpieceStateMonitor earpieceStateMonitor;
	private ScreenOrientationUpdater screenUpdater;
	
	public UserActiveState(Context context, ScreenOrientationController screenOrientationController) {
		errorHandler = new UserErrorHandler(context);
		proximityHandler = new ProximitySensorHandler(context);
		calculator = new OrientationCalculator(context);
		screenUpdater = new ScreenOrientationUpdater(calculator, screenOrientationController);
		
		RecalibrationOverlay overlay = new RecalibrationOverlay(context, screenOrientationController, calculator);
		Recalibration recalibration = new Recalibration(overlay, calculator);
		recalibrator = new RecalibrationController(proximityHandler, recalibration, errorHandler);
		
		earpieceStateMonitor = new EarpieceStateMonitor(context, callStateReceiver);
		
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
		earpieceStateMonitor.ensureStarted();
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
		
		earpieceStateMonitor.ensureStopped();
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
	
	private final Listener<EarpieceSpeakerState> callStateReceiver = new Listener<EarpieceSpeakerState>() {
		@Override
		public void stateChanged(EarpieceSpeakerState state) {
			try {
				if (!state.usingEarpieceSpeaker())
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
