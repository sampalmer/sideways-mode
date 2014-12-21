package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.components.RotationService;
import samuelpalmer.sensorautorotation.eventhandling.EventHandler;

public class UserActiveState extends State {
	
	private OrientationSensorHandler orientationListener;
	private UserErrorHandler errorHandler;
	private ScreenOrientationUpdater screenUpdater;
	
	public UserActiveState(Context context) {
		errorHandler = new UserErrorHandler(context);
		screenUpdater = new ScreenOrientationUpdater(RotationService.calculator, RotationService.screenOrientationController);
		
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
        RotationService.calculator.ensureStarted();
		orientationListener.ensureStarted();

		orientationListener.onRotationChanged().subscribe(deviceOrientationSubscriber);
	}

	@Override
	protected void stop() throws EnvironmentException {
		orientationListener.onRotationChanged().unsubscribe(deviceOrientationSubscriber);
		
		if (orientationListener != null)
			orientationListener.ensureStopped();

        RotationService.calculator.ensureStopped();
		screenUpdater.ensureStopped();		
	} 
	
	EventHandler<Orientation> deviceOrientationSubscriber = new EventHandler<Orientation>() {
		
		@Override
		public void update(Orientation event)
				throws EnvironmentException {
            RotationService.calculator.setDevice(event);
		}
	};

}
