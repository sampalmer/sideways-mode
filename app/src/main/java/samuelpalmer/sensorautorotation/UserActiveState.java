package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class UserActiveState extends State {

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
		screenUpdater = new ScreenOrientationUpdater(this.calculator, this.screenOrientationController);

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

		orientationListener.onRotationChanged().subscribe(deviceOrientationSubscriber);
	}

	@Override
	protected void stop() throws EnvironmentException {
		orientationListener.onRotationChanged().unsubscribe(deviceOrientationSubscriber);

		if (orientationListener != null)
			orientationListener.ensureStopped();

		calculator.ensureStopped();
		earpieceStateMonitor.ensureStopped();
		screenUpdater.ensureStopped();

		RotationService.overlay.ensureStopped();
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
			if (state.usingEarpieceSpeaker())
				calculator.resetUser();
		}
	};
}
