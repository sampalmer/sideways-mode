package samuelpalmer.sensorautorotation;

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
			calculator = new OrientationCalculator(context, screen);
			orientationListener = new OrientationSensorHandler(context, calculator);
				
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
				calculator.stop();
			
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
