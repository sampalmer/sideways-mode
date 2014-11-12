package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.landscapetriggering.ProximitySensorMonitorFactory;
import samuelpalmer.sensorautorotation.systemconfiguration.AndroidSystemSettingSource;
import samuelpalmer.sensorautorotation.systemconfiguration.SystemSettings;
import android.content.Context;

public class App {
	
	private Screen screen;
	private OrientationSensorHandler orientationListener;
	private UserErrorHandler errorHandler;
	private boolean destroyed;
	private OrientationCalculator calculator;
	
	public App(Context context) {
		try {
			screen = new Screen(new SystemSettings(new AndroidSystemSettingSource(context.getContentResolver())));
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
