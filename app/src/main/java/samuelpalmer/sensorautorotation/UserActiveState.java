package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class UserActiveState extends State {

	private final ProximitySensorHandler proximityHandler;
	private final RecalibrationController recalibrator;
	private final EarpieceStateMonitor earpieceStateMonitor;
	private final OrientationCalculator calculator;
	private final ScreenOrientationController screenOrientationController;
	private OrientationSensorHandler orientationListener;
	private UserErrorHandler errorHandler;
	private ScreenOrientationUpdater screenUpdater;

	public UserActiveState(Context context, OrientationCalculator calculator, ScreenOrientationController screenOrientationController) {
		this.calculator = calculator;
		this.screenOrientationController = screenOrientationController;
		errorHandler = new UserErrorHandler(context);
		proximityHandler = new ProximitySensorHandler(context);
		screenUpdater = new ScreenOrientationUpdater(this.calculator, this.screenOrientationController);
		recalibrator = new RecalibrationController(proximityHandler);

		earpieceStateMonitor = new EarpieceStateMonitor(context, callStateReceiver);

		try {
			orientationListener = new OrientationSensorHandler(context);
		} catch (EnvironmentException e) {
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

		calculator.ensureStopped();
		earpieceStateMonitor.ensureStopped();
		recalibrator.ensureStopped();
		screenUpdater.ensureStopped();
	}

	EventHandler<Orientation> deviceOrientationSubscriber = new EventHandler<Orientation>() {

		@Override
		public void update(Orientation event)
				throws EnvironmentException {
			calculator.setDevice(event);
		}
	};

	private final StateMonitor.Listener<EarpieceSpeakerState> callStateReceiver = new StateMonitor.Listener<EarpieceSpeakerState>() {
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
