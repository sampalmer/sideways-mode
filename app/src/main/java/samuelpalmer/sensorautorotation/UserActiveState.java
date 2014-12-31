package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class UserActiveState extends State {

	private final EarpieceStateMonitor earpieceStateMonitor;
	private OrientationSensorHandler orientationListener;
	private EnvironmentErrorNotifier errorHandler;
	private ScreenOrientationUpdater screenUpdater;

	public UserActiveState(Context context) {
		errorHandler = new EnvironmentErrorNotifier(context);
		screenUpdater = new ScreenOrientationUpdater(context, RotationService.calculator, RotationService.screenOrientationController);

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
		RotationService.calculator.ensureStarted();
		earpieceStateMonitor.ensureStarted();
		orientationListener.ensureStarted();

		orientationListener.onRotationChanged().subscribe(deviceOrientationSubscriber);
	}

	@Override
	protected void stop() throws EnvironmentException {
		orientationListener.onRotationChanged().unsubscribe(deviceOrientationSubscriber);

		if (orientationListener != null)
			orientationListener.ensureStopped();

		RotationService.calculator.ensureStopped();
		earpieceStateMonitor.ensureStopped();
		screenUpdater.ensureStopped();
	}

	EventHandler<Orientation> deviceOrientationSubscriber = new EventHandler<Orientation>() {

		@Override
		public void update(Orientation event)
				throws EnvironmentException {
			RotationService.calculator.setDevice(event);
		}
	};

	private final StateMonitor.Listener<EarpieceSpeakerState> callStateReceiver = new StateMonitor.Listener<EarpieceSpeakerState>() {
		@Override
		public void stateChanged(EarpieceSpeakerState state) {
			if (state.usingEarpieceSpeaker())
				RotationService.calculator.resetUser();
		}
	};
}
