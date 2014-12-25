package samuelpalmer.sensorautorotation;

import android.content.Context;

import samuelpalmer.sensorautorotation.unused.ScreenOrientationEnforcer;

public class OrientationEnforcementMonitor {

	private final ScreenOrientationEnforcer orientationEnforcer;
	private final Context context;

	public OrientationEnforcementMonitor(Context context) {
		this.context = context;
		orientationEnforcer = new ScreenOrientationEnforcer(context);
	}

	public void reportWindowChange(CharSequence packageName) {
		try {
			if (packageName.equals("com.sonyericsson.home"))
				orientationEnforcer.ensureStarted();
			else
				orientationEnforcer.ensureStopped();
		}
		catch (EnvironmentException e) {
			new UserErrorHandler(context).handle(e);
		}
	}

}
