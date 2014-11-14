package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.landscapetriggering.ProximitySensorMonitorFactory;
import android.content.Context;

public class App {
	
	private Screen screen;
	private OrientationSensorHandler orientationListener;
	private UserErrorHandler errorHandler;
	private boolean destroyed;
	private OrientationCalculator calculator;
	
	public App(Context context) {
		errorHandler = new UserErrorHandler(context);
		
		try {
			screen = new Screen(context);
			calculator = new OrientationCalculator(new ProximitySensorMonitorFactory(context), screen);
			orientationListener = new OrientationSensorHandler(context, calculator);
				
			calculator.resume();
			orientationListener.enable();
		}
		catch (EnvironmentException e) {
			errorHandler.handle(e);
		}
	}
	
	public void destroy() {
		if (!destroyed) {
			if (orientationListener != null)
				orientationListener.disable();
			
			if (calculator != null)
				calculator.pause();
			
			if (screen != null)
				try {
					screen.close();
				} catch (EnvironmentException e) {
					errorHandler.handle(e);
				}
			
			destroyed = true;
		}
	}

}
