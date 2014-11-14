package samuelpalmer.sensorautorotation;

import samuelpalmer.sensorautorotation.landscapetriggering.ProximitySensorMonitorFactory;
import android.content.Context;
import android.view.WindowManager;

public class App {
	
	private Screen screen;
	private OrientationSensorHandler orientationListener;
	private UserErrorHandler errorHandler;
	private boolean destroyed;
	private OrientationCalculator calculator;
	private Context context;
	
	public App(Context context) {
		this.context = context;
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

	public void rotationMightHaveChanged() {
		WindowManager x = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
		int rotation = x.getDefaultDisplay().getRotation();
		Orientation real = Orientation.fromAndroidSurfaceRotation(rotation);
		try {
			screen.set(real);
		} catch (EnvironmentException e) {
			errorHandler.handle(e);
		}
	}
	
}
