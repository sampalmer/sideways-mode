package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class UserActiveState extends State {

	private final EarpieceStateMonitor earpieceStateMonitor;
	private OrientationSensorHandler orientationListener;
	private ScreenOrientationUpdater screenUpdater;

	public UserActiveState(Context context) {
		screenUpdater = new ScreenOrientationUpdater(context, RotationService.calculator, RotationService.screenOrientationController);
		earpieceStateMonitor = new EarpieceStateMonitor(context, callStateReceiver);
		orientationListener = new OrientationSensorHandler(context);
	}

	@Override
	protected void start() {
		screenUpdater.ensureStarted();
		RotationService.calculator.ensureStarted();
		earpieceStateMonitor.ensureStarted();
		orientationListener.ensureStarted();

		orientationListener.onRotationChanged().subscribe(deviceOrientationSubscriber);
	}

	@Override
	protected void stop() {
		orientationListener.onRotationChanged().unsubscribe(deviceOrientationSubscriber);

		if (orientationListener != null)
			orientationListener.ensureStopped();

		RotationService.calculator.ensureStopped();
		earpieceStateMonitor.ensureStopped();
		screenUpdater.ensureStopped();
	}

	EventHandler<Orientation> deviceOrientationSubscriber = new EventHandler<Orientation>() {

		@Override
		public void update(Orientation event) {
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
