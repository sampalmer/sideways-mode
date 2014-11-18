package samuelpalmer.sensorautorotation;

import android.content.Context;

public class App {
	
	private OrientationSensorHandler orientationListener;
	private UserErrorHandler errorHandler;
	private boolean destroyed;
	private OrientationCalculator calculator;
	private ProximitySensorHandler proximityHandler;
	private RecalibrationOverlay overlay;
	
	public App(Context context, Screen screen) {
		errorHandler = new UserErrorHandler(context);
		calculator = new OrientationCalculator(context, screen);
		overlay = new RecalibrationOverlay(context);
		
		try {
			orientationListener = new OrientationSensorHandler(context, calculator);
			proximityHandler = new ProximitySensorHandler(context, new ProximitySensorHandler.Listener[] { calculator, overlay });
		}
		catch (EnvironmentException e) {
			errorHandler.handle(e);
			return;
		}
		
		orientationListener.enable();
	}
	
	public void destroy() {
		if (!destroyed) {
			if (proximityHandler != null)
				proximityHandler.stop();
			
			if (orientationListener != null)
				orientationListener.disable();
			
			overlay.stop();
			
			destroyed = true;
		}
	}

}
